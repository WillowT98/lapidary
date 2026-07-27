package name.lapidary.client.screen;

import name.lapidary.Lapidary;
import name.lapidary.client.magic.ClientMagicData;
import name.lapidary.magic.PlayerMagicData;
import name.lapidary.network.TomePurchasePayload;
import name.lapidary.progression.tome.TomeNode;
import name.lapidary.progression.tome.TomePage;
import name.lapidary.progression.tome.TomeTree;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public final class TomeScreen extends Screen {

    private static final String CALIBRATION_PAGE_ID =
            "calibration";

    private static final int MAX_PANEL_WIDTH = 420;
    private static final int MAX_PANEL_HEIGHT = 250;

    private static final int SCREEN_MARGIN = 8;

    private static final int TAB_RAIL_WIDTH = 88;
    private static final int TAB_GAP = 2;

    private static final int NODE_WIDTH = 46;
    private static final int NODE_HEIGHT = 24;

    private static final int SCHOOL_ICON_SIZE = 16;
    private static final int SCHOOL_ICON_TEXTURE_SIZE = 16;
    private static final String SCHOOL_ICON_DIRECTORY = "textures/gui/tome/schools/";

    private static final int PANEL_COLOR =
            0xEE17121C;

    private static final int INNER_COLOR =
            0xFF241B2C;

    private static final int BORDER_COLOR =
            0xFF735078;

    private static final int TAB_COLOR =
            0xFF312638;

    private static final int TAB_HOVERED_COLOR =
            0xFF5B4164;

    private static final int TAB_SELECTED_COLOR =
            0xFF84558F;

    private static final int LOCKED_COLOR =
            0xFF3D3740;

    private static final int AVAILABLE_COLOR =
            0xFF765080;

    private static final int AFFORDABLE_COLOR =
            0xFFA85ABA;

    private static final int OWNED_COLOR =
            0xFF347A59;

    private static final int SELECTED_BORDER_COLOR =
            0xFFFFE7FF;

    private static final int TEXT_COLOR =
            0xFFF3E7F5;

    private static final int MUTED_TEXT_COLOR =
            0xFFB9A8BD;

    private final BlockPos tablePosition;

    private final TomeCalibrationPanel calibrationPanel;

    private int insight;

    private List<String> purchasedNodeIds;

    private String selectedPageId =
            TomeTree.SCHOOLS_PAGE_ID;

    private TomeNode selectedNode =
            TomeTree.SCHOOLS_PAGE.root();

    private Button purchaseButton;

    public TomeScreen(
            BlockPos tablePosition,
            int insight,
            List<String> purchasedNodeIds
    ) {
        super(
                Component.translatable(
                        "screen.lapidary.tome.title"
                )
        );

        this.tablePosition =
                tablePosition.immutable();

        this.insight =
                insight;

        this.purchasedNodeIds =
                List.copyOf(
                        purchasedNodeIds
                );

        this.calibrationPanel =
                new TomeCalibrationPanel(
                        this.tablePosition,
                        ClientMagicData.get()
                );
    }

    @Override
    protected void init() {
        int buttonX =
                getContentRight() - 104;

        int buttonY =
                getPanelY()
                        + getPanelHeight()
                        - 28;

        this.purchaseButton =
                Button.builder(
                                Component.empty(),
                                button ->
                                        requestPurchase()
                        )
                        .bounds(
                                buttonX,
                                buttonY,
                                100,
                                20
                        )
                        .build();

        /*
         * Register the button for input and narration, but render it
         * manually so it remains sharp with the rest of the screen.
         */
        this.addWidget(
                purchaseButton
        );

        updatePurchaseButton();
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        /*
         * Minecraft completes its normal screen pass first.
         */
        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        /*
         * Everything drawn after this point remains sharp.
         */
        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                0xC0100D12
        );

        renderPanel(
                graphics
        );

        renderTabs(
                graphics,
                mouseX,
                mouseY
        );

        if (isCalibrationPage()) {
            calibrationPanel.render(
                    graphics,
                    this.font,
                    getCalibrationLeft(),
                    getCalibrationTop(),
                    getCalibrationRight(),
                    getCalibrationBottom(),
                    mouseX,
                    mouseY
            );
        } else {
            renderConnections(
                    graphics
            );

            renderNodes(
                    graphics
            );

            renderSelectedNodeDetails(
                    graphics
            );

            if (purchaseButton != null) {
                purchaseButton.render(
                        graphics,
                        mouseX,
                        mouseY,
                        partialTick
                );
            }
        }
    }

    private void renderPanel(
            GuiGraphics graphics
    ) {
        int panelX =
                getPanelX();

        int panelY =
                getPanelY();

        int panelWidth =
                getPanelWidth();

        int panelHeight =
                getPanelHeight();

        graphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                BORDER_COLOR
        );

        graphics.fill(
                panelX + 2,
                panelY + 2,
                panelX + panelWidth - 2,
                panelY + panelHeight - 2,
                PANEL_COLOR
        );

        graphics.fill(
                getContentLeft(),
                panelY + 32,
                getContentRight(),
                panelY + panelHeight - 38,
                INNER_COLOR
        );

        graphics.drawCenteredString(
                this.font,
                this.title,
                (
                        getContentLeft()
                                + getContentRight()
                ) / 2,
                panelY + 7,
                TEXT_COLOR
        );

        Component subtitle;

        if (isCalibrationPage()) {
            subtitle =
                    Component.translatable("tome.lapidary.page.calibration");
        } else {
            subtitle =
                    Component.translatable(
                            getCurrentPage()
                                    .translationKey()
                    );
        }

        graphics.drawCenteredString(
                this.font,
                subtitle,
                (
                        getContentLeft()
                                + getContentRight()
                ) / 2,
                panelY + 19,
                MUTED_TEXT_COLOR
        );

        Component insightText =
                Component.translatable(
                        "screen.lapidary.tome.insight",
                        insight
                );

        int insightX =
                getContentRight()
                        - this.font.width(
                        insightText
                )
                        - 4;

        graphics.drawString(
                this.font,
                insightText,
                insightX,
                panelY + 7,
                0xFFFFA3E8,
                true
        );
    }

    private void renderTabs(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        List<TomePage> unlockedPages =
                getUnlockedPages();

        /*
         * The final tab is always Staff Calibration.
         */
        int tabCount =
                unlockedPages.size() + 1;

        int tabHeight =
                getTabHeight(
                        tabCount
                );

        for (int index = 0;
             index < unlockedPages.size();
             index++) {

            TomePage page =
                    unlockedPages.get(index);

            renderTab(
                    graphics,
                    page.id(),
                    Component.translatable(
                            page.translationKey()
                    ),
                    index,
                    tabHeight,
                    mouseX,
                    mouseY
            );
        }

        renderTab(
                graphics,
                CALIBRATION_PAGE_ID,
                Component.translatable(
                        "tome.lapidary.page.calibration"
                ),
                unlockedPages.size(),
                tabHeight,
                mouseX,
                mouseY
        );
    }

    private void renderTab(
            GuiGraphics graphics,
            String pageId,
            Component label,
            int index,
            int tabHeight,
            int mouseX,
            int mouseY
    ) {
        int x =
                getTabX();

        int y =
                getTabY(
                        index,
                        tabHeight
                );

        boolean selected =
                pageId.equals(
                        selectedPageId
                );

        boolean hovered =
                mouseX >= x
                        && mouseX
                        < x
                        + TAB_RAIL_WIDTH
                        - 8
                        && mouseY >= y
                        && mouseY
                        < y
                        + tabHeight;

        int color;

        if (selected) {
            color =
                    TAB_SELECTED_COLOR;
        } else if (hovered) {
            color =
                    TAB_HOVERED_COLOR;
        } else {
            color =
                    TAB_COLOR;
        }

        graphics.fill(
                x,
                y,
                x + TAB_RAIL_WIDTH - 8,
                y + tabHeight,
                BORDER_COLOR
        );

        graphics.fill(
                x + 1,
                y + 1,
                x + TAB_RAIL_WIDTH - 9,
                y + tabHeight - 1,
                color
        );

        int textY =
                y + Math.max(
                        2,
                        (
                                tabHeight
                                        - this.font.lineHeight
                        ) / 2
                );

        graphics.drawCenteredString(
                this.font,
                label,
                x + (
                        TAB_RAIL_WIDTH - 8
                ) / 2,
                textY,
                TEXT_COLOR
        );
    }

    private void renderConnections(
            GuiGraphics graphics
    ) {
        TomePage page =
                getCurrentPage();

        for (TomeNode child :
                page.nodes()) {

            for (String prerequisiteId :
                    child.prerequisites()) {

                TomeNode parent =
                        TomeTree.getNode(
                                prerequisiteId
                        );

                /*
                 * Only draw prerequisites belonging to this page.
                 * Cross-page prerequisites control access but should
                 * not draw lines between different tabs.
                 */
                if (parent == null
                        || !parent.pageId()
                        .equals(page.id())) {

                    continue;
                }

                drawConnection(
                        graphics,
                        parent,
                        child,
                        getNodeColor(
                                child
                        )
                );
            }
        }
    }

    private void drawConnection(
            GuiGraphics graphics,
            TomeNode parent,
            TomeNode child,
            int color
    ) {
        int startX =
                getNodeCenterX(
                        parent
                );

        int startY =
                getNodeCenterY(
                        parent
                );

        int endX =
                getNodeCenterX(
                        child
                );

        int endY =
                getNodeCenterY(
                        child
                );

        int middleY =
                (
                        startY
                                + endY
                ) / 2;

        fillLine(
                graphics,
                startX,
                startY,
                startX,
                middleY,
                color
        );

        fillLine(
                graphics,
                startX,
                middleY,
                endX,
                middleY,
                color
        );

        fillLine(
                graphics,
                endX,
                middleY,
                endX,
                endY,
                color
        );
    }

    private static void fillLine(
            GuiGraphics graphics,
            int x1,
            int y1,
            int x2,
            int y2,
            int color
    ) {
        int minimumX =
                Math.min(
                        x1,
                        x2
                );

        int maximumX =
                Math.max(
                        x1,
                        x2
                );

        int minimumY =
                Math.min(
                        y1,
                        y2
                );

        int maximumY =
                Math.max(
                        y1,
                        y2
                );

        graphics.fill(
                minimumX - 1,
                minimumY - 1,
                maximumX + 2,
                maximumY + 2,
                color
        );
    }

    private void renderNodes(
            GuiGraphics graphics
    ) {
        for (TomeNode node :
                getCurrentPage().nodes()) {

            int x =
                    getNodeLeftX(
                            node
                    );

            int y =
                    getNodeTopY(
                            node
                    );

            boolean selected =
                    selectedNode != null
                            && node.id().equals(
                            selectedNode.id()
                    );

            int borderColor =
                    selected
                            ? SELECTED_BORDER_COLOR
                            : BORDER_COLOR;

            graphics.fill(
                    x - 2,
                    y - 2,
                    x + NODE_WIDTH + 2,
                    y + NODE_HEIGHT + 2,
                    borderColor
            );

            graphics.fill(
                    x,
                    y,
                    x + NODE_WIDTH,
                    y + NODE_HEIGHT,
                    getNodeColor(
                            node
                    )
            );

            if (isSchoolUnlockNode(node)) {
                renderSchoolIcon(
                        graphics,
                        node,
                        x,
                        y
                );
            } else {
                Component nodeText;

                if (node.root()) {
                    nodeText =
                            Component.literal(
                                    "◆"
                            );
                } else {
                    nodeText =
                            Component.literal(
                                    Integer.toString(
                                            node.cost()
                                    )
                            );
                }

                graphics.drawCenteredString(
                        this.font,
                        nodeText,
                        x + NODE_WIDTH / 2,
                        y + 8,
                        TEXT_COLOR
                );
            }
        }
    }
    /**
     * School-unlock nodes use their associated school icon instead of
     * displaying their Insight cost inside the node.
     */
    private boolean isSchoolUnlockNode(
            TomeNode node
    ) {
        return TomeTree.SCHOOLS_PAGE_ID.equals(
                getCurrentPage().id()
        ) && !node.root();
    }

    /**
     * Draws a school icon centered inside its Tome node.
     */
    private void renderSchoolIcon(
            GuiGraphics graphics,
            TomeNode node,
            int nodeX,
            int nodeY
    ) {
        ResourceLocation texture =
                getSchoolIconTexture(
                        node
                );

        int iconX =
                nodeX
                        + (
                        NODE_WIDTH
                                - SCHOOL_ICON_SIZE
                ) / 2;

        int iconY =
                nodeY
                        + (
                        NODE_HEIGHT
                                - SCHOOL_ICON_SIZE
                ) / 2;

        graphics.blit(
                texture,
                iconX,
                iconY,
                0.0F,
                0.0F,
                SCHOOL_ICON_SIZE,
                SCHOOL_ICON_SIZE,
                SCHOOL_ICON_TEXTURE_SIZE,
                SCHOOL_ICON_TEXTURE_SIZE
        );
    }

    /**
     * Converts an ID such as "schools/summoning" into:
     *
     * lapidary:textures/gui/tome/schools/summoning.png
     */
    private static ResourceLocation getSchoolIconTexture(
            TomeNode node
    ) {
        String nodeId =
                node.id();

        int separator =
                nodeId.lastIndexOf('/');

        String schoolName =
                separator >= 0
                        ? nodeId.substring(
                        separator + 1
                )
                        : nodeId;

        return Lapidary.id(
                SCHOOL_ICON_DIRECTORY
                        + schoolName
                        + ".png"
        );
    }

    private void renderSelectedNodeDetails(
            GuiGraphics graphics
    ) {
        if (selectedNode == null) {
            return;
        }

        int panelY =
                getPanelY();

        int detailY =
                panelY
                        + getPanelHeight()
                        - 28;

        Component name =
                Component.translatable(
                        selectedNode
                                .translationKey()
                );

        graphics.drawString(
                this.font,
                name,
                getContentLeft() + 4,
                detailY,
                TEXT_COLOR,
                true
        );

        graphics.drawString(
                this.font,
                getSelectedStatus(),
                getContentLeft() + 4,
                detailY + 12,
                MUTED_TEXT_COLOR,
                false
        );
    }

    private Component getSelectedStatus() {
        if (selectedNode == null) {
            return Component.empty();
        }

        if (selectedNode.root()) {
            return Component.translatable(
                    "screen.lapidary.tome.status.root"
            );
        }

        if (TomeTree.isOwned(
                purchasedNodeIds,
                selectedNode
        )) {
            return Component.translatable(
                    "screen.lapidary.tome.status.purchased"
            );
        }

        if (!TomeTree.prerequisitesMet(
                purchasedNodeIds,
                selectedNode
        )) {
            return Component.translatable(
                    "screen.lapidary.tome.status.locked"
            );
        }

        if (insight < selectedNode.cost()) {
            return Component.translatable(
                    "screen.lapidary.tome.status.expensive"
            );
        }

        return Component.translatable(
                "screen.lapidary.tome.status.available"
        );
    }

    private int getNodeColor(
            TomeNode node
    ) {
        if (TomeTree.isOwned(
                purchasedNodeIds,
                node
        )) {
            return OWNED_COLOR;
        }

        if (!TomeTree.prerequisitesMet(
                purchasedNodeIds,
                node
        )) {
            return LOCKED_COLOR;
        }

        if (insight >= node.cost()) {
            return AFFORDABLE_COLOR;
        }

        return AVAILABLE_COLOR;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0) {
            String clickedPageId =
                    findTabIdAt(
                            mouseX,
                            mouseY
                    );

            if (clickedPageId != null) {
                switchPage(
                        clickedPageId
                );

                return true;
            }
        }

        if (isCalibrationPage()) {
            if (calibrationPanel.mouseClicked(
                    mouseX,
                    mouseY,
                    button,
                    getCalibrationLeft(),
                    getCalibrationTop(),
                    getCalibrationRight(),
                    getCalibrationBottom()
            )) {
                return true;
            }

            return super.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            );
        }

        if (button == 0) {
            TomeNode clickedNode =
                    findNodeAt(
                            mouseX,
                            mouseY
                    );

            if (clickedNode != null) {
                selectedNode =
                        clickedNode;

                updatePurchaseButton();

                return true;
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseReleased(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (isCalibrationPage()
                && calibrationPanel.mouseReleased(
                mouseX,
                mouseY,
                button,
                getCalibrationLeft(),
                getCalibrationTop(),
                getCalibrationRight(),
                getCalibrationBottom()
        )) {
            return true;
        }

        return super.mouseReleased(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (isCalibrationPage()
                && calibrationPanel.mouseScrolled(
                mouseX,
                mouseY,
                scrollY,
                getCalibrationLeft(),
                getCalibrationTop(),
                getCalibrationRight(),
                getCalibrationBottom()
        )) {
            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        );
    }

    private String findTabIdAt(
            double mouseX,
            double mouseY
    ) {
        List<TomePage> unlockedPages =
                getUnlockedPages();

        int tabCount =
                unlockedPages.size() + 1;

        int tabHeight =
                getTabHeight(
                        tabCount
                );

        for (int index = 0;
             index < tabCount;
             index++) {

            int x =
                    getTabX();

            int y =
                    getTabY(
                            index,
                            tabHeight
                    );

            boolean inside =
                    mouseX >= x
                            && mouseX
                            < x
                            + TAB_RAIL_WIDTH
                            - 8
                            && mouseY >= y
                            && mouseY
                            < y
                            + tabHeight;

            if (!inside) {
                continue;
            }

            if (index == unlockedPages.size()) {
                return CALIBRATION_PAGE_ID;
            }

            return unlockedPages
                    .get(index)
                    .id();
        }

        return null;
    }

    private TomeNode findNodeAt(
            double mouseX,
            double mouseY
    ) {
        if (isCalibrationPage()) {
            return null;
        }

        for (TomeNode node :
                getCurrentPage().nodes()) {

            int x =
                    getNodeLeftX(
                            node
                    );

            int y =
                    getNodeTopY(
                            node
                    );

            if (mouseX >= x
                    && mouseX < x + NODE_WIDTH
                    && mouseY >= y
                    && mouseY < y + NODE_HEIGHT) {

                return node;
            }
        }

        return null;
    }

    private void switchPage(
            String pageId
    ) {
        if (pageId.equals(
                selectedPageId
        )) {
            return;
        }

        if (CALIBRATION_PAGE_ID.equals(
                pageId
        )) {
            selectedPageId =
                    CALIBRATION_PAGE_ID;

            updatePurchaseButton();
            playPageTurnSound();

            return;
        }

        TomePage page =
                TomeTree.getPage(
                        pageId
                );

        if (page == null) {
            return;
        }

        if (!TomeTree.isPageUnlocked(
                purchasedNodeIds,
                page
        )) {
            return;
        }

        selectedPageId =
                page.id();

        selectedNode =
                page.root();

        updatePurchaseButton();
        playPageTurnSound();
    }

    private void playPageTurnSound() {
        if (this.minecraft == null) {
            return;
        }

        this.minecraft
                .getSoundManager()
                .play(
                        SimpleSoundInstance.forUI(
                                SoundEvents.BOOK_PAGE_TURN,
                                1.0F
                        )
                );
    }

    private void requestPurchase() {
        if (!canPurchaseSelectedNode()) {
            return;
        }

        purchaseButton.active =
                false;

        ClientPlayNetworking.send(
                new TomePurchasePayload(
                        tablePosition,
                        selectedNode.id()
                )
        );
    }

    private boolean canPurchaseSelectedNode() {
        if (isCalibrationPage()) {
            return false;
        }

        if (selectedNode == null
                || selectedNode.root()) {

            return false;
        }

        TomePage page =
                getCurrentPage();

        if (!TomeTree.isPageUnlocked(
                purchasedNodeIds,
                page
        )) {
            return false;
        }

        if (TomeTree.isOwned(
                purchasedNodeIds,
                selectedNode
        )) {
            return false;
        }

        if (!TomeTree.prerequisitesMet(
                purchasedNodeIds,
                selectedNode
        )) {
            return false;
        }

        return insight >= selectedNode.cost();
    }

    private void updatePurchaseButton() {
        if (purchaseButton == null) {
            return;
        }

        purchaseButton.visible =
                !isCalibrationPage();

        if (isCalibrationPage()) {
            purchaseButton.active =
                    false;

            return;
        }

        if (selectedNode == null
                || selectedNode.root()) {

            purchaseButton.setMessage(
                    Component.translatable(
                            "screen.lapidary.tome.button.root"
                    )
            );

            purchaseButton.active =
                    false;

            return;
        }

        if (TomeTree.isOwned(
                purchasedNodeIds,
                selectedNode
        )) {
            purchaseButton.setMessage(
                    Component.translatable(
                            "screen.lapidary.tome.button.purchased"
                    )
            );

            purchaseButton.active =
                    false;

            return;
        }

        if (!TomeTree.prerequisitesMet(
                purchasedNodeIds,
                selectedNode
        )) {
            purchaseButton.setMessage(
                    Component.translatable(
                            "screen.lapidary.tome.button.locked"
                    )
            );

            purchaseButton.active =
                    false;

            return;
        }

        purchaseButton.setMessage(
                Component.translatable(
                        "screen.lapidary.tome.button.purchase",
                        selectedNode.cost()
                )
        );

        purchaseButton.active =
                insight >= selectedNode.cost();
    }

    /**
     * Applies authoritative Tome progression state sent by the server.
     */
    public void updateState(
            int insight,
            List<String> purchasedNodeIds
    ) {
        this.insight =
                insight;

        this.purchasedNodeIds =
                List.copyOf(
                        purchasedNodeIds
                );

        /*
         * Calibration is not a progression-tree page, so it should
         * remain open when the progression state changes.
         */
        if (!isCalibrationPage()) {
            TomePage currentPage =
                    TomeTree.getPage(
                            selectedPageId
                    );

            /*
             * A reset can remove the school currently being viewed.
             */
            if (currentPage == null
                    || !TomeTree.isPageUnlocked(
                    this.purchasedNodeIds,
                    currentPage
            )) {
                selectedPageId =
                        TomeTree.SCHOOLS_PAGE_ID;

                selectedNode =
                        TomeTree.SCHOOLS_PAGE
                                .root();
            }
        }

        updatePurchaseButton();
    }

    /**
     * Applies authoritative player magic data sent by the server.
     */
    public void updateMagicData(
            PlayerMagicData newData
    ) {
        calibrationPanel.updateData(
                newData
        );
    }

    private boolean isCalibrationPage() {
        return CALIBRATION_PAGE_ID.equals(
                selectedPageId
        );
    }

    private TomePage getCurrentPage() {
        TomePage page =
                TomeTree.getPage(
                        selectedPageId
                );

        return page != null
                ? page
                : TomeTree.SCHOOLS_PAGE;
    }

    private List<TomePage> getUnlockedPages() {
        return TomeTree.getUnlockedPages(
                purchasedNodeIds
        );
    }

    private int getPanelWidth() {
        return Math.min(
                MAX_PANEL_WIDTH,
                Math.max(
                        1,
                        this.width
                                - SCREEN_MARGIN * 2
                )
        );
    }

    private int getPanelHeight() {
        return Math.min(
                MAX_PANEL_HEIGHT,
                Math.max(
                        1,
                        this.height
                                - SCREEN_MARGIN * 2
                )
        );
    }

    private int getPanelX() {
        return (
                this.width
                        - getPanelWidth()
        ) / 2;
    }

    private int getPanelY() {
        return (
                this.height
                        - getPanelHeight()
        ) / 2;
    }

    private int getTabX() {
        return getPanelX() + 4;
    }

    private int getTabHeight(
            int tabCount
    ) {
        if (tabCount <= 0) {
            return 16;
        }

        int availableHeight =
                getPanelHeight() - 40;

        int gaps =
                Math.max(
                        0,
                        tabCount - 1
                ) * TAB_GAP;

        return Math.max(
                12,
                Math.min(
                        17,
                        (
                                availableHeight
                                        - gaps
                        ) / tabCount
                )
        );
    }

    private int getTabY(
            int index,
            int tabHeight
    ) {
        return getPanelY()
                + 30
                + index
                * (
                tabHeight
                        + TAB_GAP
        );
    }

    private int getContentLeft() {
        return getPanelX()
                + TAB_RAIL_WIDTH;
    }

    private int getContentRight() {
        return getPanelX()
                + getPanelWidth()
                - 6;
    }

    private int getCalibrationLeft() {
        return getContentLeft() + 8;
    }

    private int getCalibrationTop() {
        return getPanelY() + 42;
    }

    private int getCalibrationRight() {
        return getContentRight() - 8;
    }

    private int getCalibrationBottom() {
        return getPanelY()
                + getPanelHeight()
                - 44;
    }

    private int getTreeOriginX() {
        return (
                getContentLeft()
                        + getContentRight()
        ) / 2;
    }

    private int getTreeOriginY() {
        int treeTop =
                getPanelY() + 42;

        int treeBottom =
                getPanelY()
                        + getPanelHeight()
                        - 42;

        return (
                treeTop
                        + treeBottom
        ) / 2;
    }

    private double getHorizontalTreeScale() {
        int contentWidth =
                getContentRight()
                        - getContentLeft();

        double availableScale =
                (
                        contentWidth
                                - NODE_WIDTH
                                - 12.0D
                ) / 220.0D;

        return Math.max(
                0.50D,
                Math.min(
                        1.0D,
                        availableScale
                )
        );
    }

    private double getVerticalTreeScale() {
        int treeHeight =
                getPanelHeight() - 84;

        double availableScale =
                (
                        treeHeight
                                - NODE_HEIGHT
                                - 8.0D
                ) / 160.0D;

        return Math.max(
                0.45D,
                Math.min(
                        1.0D,
                        availableScale
                )
        );
    }

    private int getNodeLeftX(
            TomeNode node
    ) {
        int centerX =
                getTreeOriginX()
                        + (
                        int
                        ) Math.round(
                        node.x()
                                * getHorizontalTreeScale()
                );

        return centerX
                - NODE_WIDTH / 2;
    }

    private int getNodeTopY(
            TomeNode node
    ) {
        int centerY =
                getTreeOriginY()
                        + (
                        int
                        ) Math.round(
                        node.y()
                                * getVerticalTreeScale()
                );

        return centerY
                - NODE_HEIGHT / 2;
    }

    private int getNodeCenterX(
            TomeNode node
    ) {
        return getNodeLeftX(
                node
        )
                + NODE_WIDTH / 2;
    }

    private int getNodeCenterY(
            TomeNode node
    ) {
        return getNodeTopY(
                node
        )
                + NODE_HEIGHT / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
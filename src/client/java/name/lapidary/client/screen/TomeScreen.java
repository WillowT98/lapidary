package name.lapidary.client.screen;

import name.lapidary.network.TomePurchasePayload;
import name.lapidary.progression.tome.TomeNode;
import name.lapidary.progression.tome.TomeTree;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public final class TomeScreen extends Screen {

    /*
     * These are maximum panel dimensions.
     *
     * The actual panel becomes smaller when the player's current
     * GUI-scaled screen is smaller than these values.
     */
    private static final int MAX_PANEL_WIDTH = 420;
    private static final int MAX_PANEL_HEIGHT = 250;

    /*
     * The panel will always attempt to leave this much room between
     * itself and the edges of the screen.
     */
    private static final int SCREEN_MARGIN = 8;

    private static final int NODE_WIDTH = 46;
    private static final int NODE_HEIGHT = 24;

    private static final int PANEL_COLOR =
            0xEE17121C;

    private static final int INNER_COLOR =
            0xFF241B2C;

    private static final int BORDER_COLOR =
            0xFF735078;

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

    /*
     * The table position is sent back with purchase requests so the
     * server can verify that the player is still close to a real
     * Tome Table.
     */
    private final BlockPos tablePosition;

    /*
     * These values begin with the authoritative state sent by the
     * server when the screen opens.
     *
     * They are refreshed whenever the server responds to a purchase
     * request.
     */
    private int insight;
    private long purchasedMask;

    private TomeNode selectedNode =
            TomeTree.ROOT;

    private Button purchaseButton;

    public TomeScreen(
            BlockPos tablePosition,
            int insight,
            long purchasedMask
    ) {
        super(
                Component.translatable(
                        "screen.lapidary.tome.title"
                )
        );

        this.tablePosition =
                tablePosition;

        this.insight =
                insight;

        this.purchasedMask =
                purchasedMask;
    }

    @Override
    protected void init() {
        int panelX =
                getPanelX();

        int panelY =
                getPanelY();

        int panelWidth =
                getPanelWidth();

        int panelHeight =
                getPanelHeight();

        this.purchaseButton =
                Button.builder(
                                Component.empty(),
                                button ->
                                        requestPurchase()
                        )
                        .bounds(
                                panelX
                                        + panelWidth
                                        - 112,
                                panelY
                                        + panelHeight
                                        - 28,
                                100,
                                20
                        )
                        .build();

        /*
         * Register the button for input and narration, but render it
         * manually after the rest of the Tome interface.
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
         * Let Minecraft perform its normal screen rendering first.
         *
         * Previously, this was called after the custom Tome graphics,
         * causing Minecraft's screen background pass to affect them.
         */
        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        /*
         * Draw a simple dark overlay over the world.
         *
         * Everything drawn after this point remains crisp.
         */
        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                0xC0100D12
        );

        renderPanel(graphics);
        renderConnections(graphics);
        renderNodes(graphics);
        renderSelectedNodeDetails(graphics);

        /*
         * The button was registered with addWidget rather than
         * addRenderableWidget, so we draw it manually at the end.
         * This ensures it appears above the Tome panel.
         */
        if (purchaseButton != null) {
            purchaseButton.render(
                    graphics,
                    mouseX,
                    mouseY,
                    partialTick
            );
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

        /*
         * Outer border.
         */
        graphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                BORDER_COLOR
        );

        /*
         * Main panel.
         */
        graphics.fill(
                panelX + 2,
                panelY + 2,
                panelX + panelWidth - 2,
                panelY + panelHeight - 2,
                PANEL_COLOR
        );

        /*
         * Inner tree area.
         */
        graphics.fill(
                panelX + 8,
                panelY + 28,
                panelX + panelWidth - 8,
                panelY + panelHeight - 38,
                INNER_COLOR
        );

        graphics.drawCenteredString(
                this.font,
                this.title,
                panelX + panelWidth / 2,
                panelY + 10,
                TEXT_COLOR
        );

        Component insightText =
                Component.translatable(
                        "screen.lapidary.tome.insight",
                        insight
                );

        graphics.drawString(
                this.font,
                insightText,
                panelX + 12,
                panelY + 12,
                0xFFFFA3E8,
                true
        );

        graphics.drawCenteredString(
                this.font,
                Component.translatable(
                        "screen.lapidary.tome.branch_a"
                ),
                panelX + panelWidth / 4,
                panelY + 34,
                MUTED_TEXT_COLOR
        );

        graphics.drawCenteredString(
                this.font,
                Component.translatable(
                        "screen.lapidary.tome.branch_b"
                ),
                panelX + panelWidth * 3 / 4,
                panelY + 34,
                MUTED_TEXT_COLOR
        );
    }

    private void renderConnections(
            GuiGraphics graphics
    ) {
        for (TomeNode child :
                TomeTree.NODES) {

            for (int prerequisiteIndex :
                    child.prerequisites()) {

                TomeNode parent =
                        TomeTree.getByIndex(
                                prerequisiteIndex
                        );

                if (parent == null) {
                    continue;
                }

                /*
                 * The connector uses the state color of the child node.
                 */
                int color =
                        getNodeColor(
                                child
                        );

                drawConnection(
                        graphics,
                        parent,
                        child,
                        color
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
                getNodeTopY(
                        parent
                ) + NODE_HEIGHT;

        int endX =
                getNodeCenterX(
                        child
                );

        int endY =
                getNodeTopY(
                        child
                );

        int middleY =
                (startY + endY) / 2;

        /*
         * Draw an advancement-style elbow connector:
         *
         *      |
         *      +-----
         *            |
         */
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
                TomeTree.NODES) {

            int x =
                    getNodeLeftX(
                            node
                    );

            int y =
                    getNodeTopY(
                            node
                    );

            boolean selected =
                    node.index()
                            == selectedNode.index();

            int borderColor =
                    selected
                            ? SELECTED_BORDER_COLOR
                            : BORDER_COLOR;

            /*
             * Node border.
             */
            graphics.fill(
                    x - 2,
                    y - 2,
                    x + NODE_WIDTH + 2,
                    y + NODE_HEIGHT + 2,
                    borderColor
            );

            /*
             * Node interior.
             */
            graphics.fill(
                    x,
                    y,
                    x + NODE_WIDTH,
                    y + NODE_HEIGHT,
                    getNodeColor(
                            node
                    )
            );

            Component nodeText;

            if (node.root()) {
                nodeText =
                        Component.translatable(
                                "screen.lapidary.tome.root"
                        );
            } else {
                /*
                 * Dummy nodes currently display only their Insight price.
                 */
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

    private void renderSelectedNodeDetails(
            GuiGraphics graphics
    ) {
        int panelX =
                getPanelX();

        int panelY =
                getPanelY();

        int panelHeight =
                getPanelHeight();

        Component name =
                Component.translatable(
                        selectedNode
                                .translationKey()
                );

        graphics.drawString(
                this.font,
                name,
                panelX + 12,
                panelY + panelHeight - 28,
                TEXT_COLOR,
                true
        );

        Component status =
                getSelectedStatus();

        graphics.drawString(
                this.font,
                status,
                panelX + 12,
                panelY + panelHeight - 16,
                MUTED_TEXT_COLOR,
                false
        );
    }

    private Component getSelectedStatus() {
        if (selectedNode.root()) {
            return Component.translatable(
                    "screen.lapidary.tome.status.root"
            );
        }

        if (TomeTree.isOwned(
                purchasedMask,
                selectedNode
        )) {
            return Component.translatable(
                    "screen.lapidary.tome.status.purchased"
            );
        }

        if (!TomeTree.prerequisitesMet(
                purchasedMask,
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
                purchasedMask,
                node
        )) {
            return OWNED_COLOR;
        }

        if (!TomeTree.prerequisitesMet(
                purchasedMask,
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
            TomeNode clickedNode =
                    findNodeAt(
                            mouseX,
                            mouseY
                    );

            if (clickedNode != null) {
                this.selectedNode =
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

    private TomeNode findNodeAt(
            double mouseX,
            double mouseY
    ) {
        for (TomeNode node :
                TomeTree.NODES) {

            int x =
                    getNodeLeftX(
                            node
                    );

            int y =
                    getNodeTopY(
                            node
                    );

            boolean insideNode =
                    mouseX >= x
                            && mouseX < x + NODE_WIDTH
                            && mouseY >= y
                            && mouseY < y + NODE_HEIGHT;

            if (insideNode) {
                return node;
            }
        }

        return null;
    }

    private void requestPurchase() {
        if (!canPurchaseSelectedNode()) {
            return;
        }

        /*
         * Disable the button until the authoritative server response
         * arrives. This also prevents rapid duplicate requests.
         */
        purchaseButton.active =
                false;

        ClientPlayNetworking.send(
                new TomePurchasePayload(
                        tablePosition,
                        selectedNode.index()
                )
        );
    }

    private boolean canPurchaseSelectedNode() {
        if (selectedNode == null
                || selectedNode.root()) {

            return false;
        }

        if (TomeTree.isOwned(
                purchasedMask,
                selectedNode
        )) {
            return false;
        }

        if (!TomeTree.prerequisitesMet(
                purchasedMask,
                selectedNode
        )) {
            return false;
        }

        return insight
                >= selectedNode.cost();
    }

    private void updatePurchaseButton() {
        if (purchaseButton == null) {
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
                purchasedMask,
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
                purchasedMask,
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
                insight
                        >= selectedNode.cost();
    }

    /**
     * Called by the client networking handler when the server returns
     * an authoritative Tome state.
     */
    public void updateState(
            int insight,
            long purchasedMask
    ) {
        this.insight =
                insight;

        this.purchasedMask =
                purchasedMask;

        updatePurchaseButton();
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

    private int getTreeOriginX() {
        return getPanelX()
                + getPanelWidth() / 2;
    }

    /*
     * Compress horizontal distances between nodes when the GUI-scaled
     * screen is narrow.
     *
     * The node rectangles and text remain at native size, which keeps
     * them sharper than scaling the whole interface.
     */
    private double getHorizontalTreeScale() {
        double availableScale =
                (
                        getPanelWidth()
                                - 70.0D
                ) / 350.0D;

        return Math.max(
                0.45D,
                Math.min(
                        1.0D,
                        availableScale
                )
        );
    }

    /*
     * Compress vertical distances when the screen is short.
     *
     * Even at the maximum panel height, the spacing is slightly
     * compressed to leave room for the title and controls.
     */
    private double getVerticalTreeScale() {
        double availableScale =
                (
                        getPanelHeight()
                                - 114.0D
                ) / 170.0D;

        return Math.max(
                0.35D,
                Math.min(
                        0.80D,
                        availableScale
                )
        );
    }

    private int getTreeOriginY() {
        double verticalScale =
                getVerticalTreeScale();

        /*
         * The root's configured Y position is -90.
         *
         * This places the root near the top of the inner tree area
         * regardless of how much the tree has been compressed.
         */
        return getPanelY()
                + 45
                + (int) Math.round(
                90.0D
                        * verticalScale
        );
    }

    private int getNodeLeftX(
            TomeNode node
    ) {
        int scaledX =
                (int) Math.round(
                        node.x()
                                * getHorizontalTreeScale()
                );

        return getTreeOriginX()
                + scaledX
                - NODE_WIDTH / 2;
    }

    private int getNodeTopY(
            TomeNode node
    ) {
        int scaledY =
                (int) Math.round(
                        node.y()
                                * getVerticalTreeScale()
                );

        return getTreeOriginY()
                + scaledY;
    }

    private int getNodeCenterX(
            TomeNode node
    ) {
        return getNodeLeftX(
                node
        ) + NODE_WIDTH / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
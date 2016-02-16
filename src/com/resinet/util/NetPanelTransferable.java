package com.resinet.util;

import java.awt.datatransfer.*;
import java.io.IOException;

class NetPanelTransferable implements Transferable, ClipboardOwner {
    public static final DataFlavor DATA_FLAVOR = new DataFlavor(NodeEdgeWrapper.class, "Nodes and Edges");

    private final NodeEdgeWrapper copyData;

    public NetPanelTransferable(NodeEdgeWrapper wrapper) {
        copyData = wrapper;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //ist egal
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DATA_FLAVOR);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DATA_FLAVOR)) {
            return copyData;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public NodeEdgeWrapper getTransferData() {
        return copyData;
    }
}

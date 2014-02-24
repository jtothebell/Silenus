package com.silenistudios.silenus.dom.fillstyles;

import java.util.Vector;

import com.silenistudios.silenus.ParseException;
import com.silenistudios.silenus.ShapeRenderInterface;
import com.silenistudios.silenus.raw.TransformationMatrix;
import com.silenistudios.silenus.xml.Node;
import com.silenistudios.silenus.xml.XMLUtility;


/**
 * Now this is probably the most curious thing I've encountered so far in the XFL format, and there are quite a few
 * things that make no sense at all. In the XML format, a linear gradient is represented by a transformation matrix,
 * from which somehow the begin- and endpoints of the gradient must be derived.
 * After some puzzling and empirical estimation, I found that after transforming [-1000,0] and [1000,0],
 * you get something close to the begin- and endpoints of the gradient. Don't ask me why.
 * @author Karel
 *
 */
public class RadialGradient implements Paint {

    // begin point
    double fCenterX;
    double fCenterY;

    double fRadius;

    // color stops
    Vector<ColorStop> fColorStops = new Vector<ColorStop>();


    // construct the linear gradient from XML
    public RadialGradient(XMLUtility XMLUtility, Node root) throws ParseException {

        // get the transformation matrix
        Node matrixNode = XMLUtility.findNode(root,  "Matrix");
        TransformationMatrix m = new TransformationMatrix(XMLUtility, matrixNode);

        fCenterX = m.getTranslateX();
        fCenterY = m.getTranslateY();

        fRadius =  819.2 * m.getMatrix()[0][0]; //a


        // get the gradient entries
        Vector<Node> nodes = XMLUtility.findNodes(root,  "GradientEntry");
        for (Node node : nodes) {
            double ratio = XMLUtility.getDoubleAttribute(node, "ratio", 0.0);
            String colorCode = XMLUtility.getAttribute(node,  "color", "#000000");
            Color color = Color.parseColor(colorCode);
            color.setAlpha(XMLUtility.getDoubleAttribute(node, "alpha", 1.0));
            fColorStops.add(new ColorStop(ratio, color));
        }
    }

    @Override
    public void render(ShapeRenderInterface renderer) {
        renderer.fillRadialGradient(fCenterX, fCenterY, fRadius, fColorStops);
    }

    @Override
    public String getJSON() {
        StringBuilder ss = new StringBuilder();
        ss.append("{");
        ss.append("\"type\":\"RadialGradient\",");
        ss.append("\"center\":[").append(fCenterX).append(",").append(fCenterY).append("],");
        ss.append("\"radius\":").append(fRadius).append(",");
        ss.append("\"colorStops\":[");
        for (int i = 0; i < fColorStops.size(); ++i) {
            if (i != 0) ss.append(",");
            ColorStop stop = fColorStops.get(i);
            ss.append("{");
            ss.append("\"ratio\":").append(stop.getRatio()).append(",");
            ss.append("\"red\":").append(stop.getColor().getRed()).append(",");
            ss.append("\"green\":").append(stop.getColor().getGreen()).append(",");
            ss.append("\"blue\":").append(stop.getColor().getBlue()).append(",");
            ss.append("\"alpha\":").append(stop.getColor().getAlpha());
            ss.append("}");
        }
        ss.append("]");
        ss.append("}");
        return ss.toString();
    }

}

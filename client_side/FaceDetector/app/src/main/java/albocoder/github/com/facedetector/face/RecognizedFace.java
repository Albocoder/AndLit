package albocoder.github.com.facedetector.face;

public class RecognizedFace {
    private Face f;
    private int [] labels;
    private double [] confidence;

    public RecognizedFace(Face f, int [] l, double [] c){
        if (l.length != c.length)
            throw new RuntimeException("labels must be same number as confidences");
        if(l.length <= 0 || c.length < 0)
            throw new RuntimeException("must have at least 1 label");
        this.f = f;
        labels = l;
        confidence = c;
    }

    public int [] getLabels(){return labels;}
    public double[] getConfidences(){return confidence;}
    public Face getFace(){return f;}

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("< (label,confidence) = { ");
        for (int i = 0; i < labels.length; i++)
            sb.append("("+labels[i]+","+confidence[i]+") ");
        sb.append("}");
        return sb.toString();
    }
}

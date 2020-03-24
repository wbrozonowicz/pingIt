package sample;

public class PingResult {
    private double avg;
    private double max;
    private double min;
    private double sum;
    private int count;


    public PingResult(double avg, double max, double min) {
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.sum=0;
        this.count=0;
    }


    public void setSum(double add) {
        this.sum+=add;
    }

    public void setCount() {
        this.count++;
    }


    public double getAvg() {
        return avg;
    }

    public void setAvg() {
        if (count>0)
        this.avg = this.sum/this.count;
    }

    public void setMin(double resp){
        if(min==0){
            this.min=resp;
        } else {
            if (this.min>resp)
                    this.min = resp;
        }
    }


    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }


    @Override
    public String toString() {
        return "PingResult{" +
                "avg=" + avg +
                ", max=" + max +
                ", min=" + min +
                '}';
    }
}

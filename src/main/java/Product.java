public class Product {

    int id;
    int stockid;
    String s_date;
    String stime;
    String etime;
    //场地号
    String sname;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStockid() {
        return stockid;
    }

    public void setStockid(int stockid) {
        this.stockid = stockid;
    }

    public String getS_date() {
        return s_date;
    }

    public void setS_date(String s_date) {
        this.s_date = s_date;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getEtime() {
        return etime;
    }

    public void setEtime(String etime) {
        this.etime = etime;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    @Override
    public String toString(){
        return "[" + this.getId() + "," + this.getStockid() + "," + this.getS_date() + "," + this.getStime() + "," + this.getEtime() + ",场地：" + this.getSname() + "]";
    }

    public String getPath(){
        String filePath = this.getClass().getClassLoader().getResource("chromedriver").getPath();
        return filePath;
    }
}

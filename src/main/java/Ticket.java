import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Ticket {

    static int status = 0;
    private static final Logger logger = LogManager.getLogger("Ticket");

    public static void main(String[] args) {

        //id=35是东校区羽毛球场
        //下面是查询哪些空场的api
        //http://gym.sysu.edu.cn/product/findOkArea.html?s_date=2018-09-19&serviceid=35
        Product path = new Product();
        String fileName = path.getPath();
        System.out.println("驱动的路径是：" + fileName);
        System.setProperty("webdriver.chrome.driver", fileName);
        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入登录账号：");
        String loginid = scanner.nextLine();
        System.out.println("请输入密码：");
        String password = scanner.nextLine();
        System.out.println("请输入要抢的日期：（格式：2018-09-02）");
        String date = scanner.nextLine();
        System.out.println("请输入要抢的场数：");
        int times = Integer.parseInt(scanner.nextLine());
        System.out.println("请输入抢票id：（东校区羽毛球场id是：35）");
        String id = scanner.nextLine();
        WebDriver driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1680,1080));
        boolean flag = true;
        /**
         *  status的值代表:
         *  0：还没抢到票
         *  1：抢到了19:00-20:00的票
         *  2：抢到了20:00-21:00的票
         *  3：抢到了18:00-19:00的票
         */
        while(flag){
            try {
                login(Integer.parseInt(id), loginid, password, driver);
            } catch (Exception e) {
                logger.error("登录失败，现在重新登录！");
                continue;
            }
            flag = false;
        }

        int tempStatus = 0;
        int count = 0;
        while (count < times) {
            try {
                tempStatus = status;
                order(date, Integer.parseInt(id), driver);
            } catch (Exception e) {
                logger.error("抢场失败，现在重抢！");
                status = tempStatus;
                continue;
            }
            count++;
        }
        logger.info("抢场完成！请自行到订单管理里付款！");
    }

    /**
     * 获取可以预定的场次
     * @param date
     * @param id
     * @return
     * @throws Exception
     */
    public static List<Product> getOkArea(String date, int id) throws Exception {
        String url = "http://gym.sysu.edu.cn/product/findOkArea.html?s_date=" + date + "&serviceid=" + id;
        Document doc;
        List<Product> list = new ArrayList<Product>();
        doc = Jsoup.connect(url).get();
        Element body = doc.body();
        JSONObject json = JSONObject.parseObject(body.text());
        JSONArray array = json.getJSONArray("object");
        if (array == null) {
            return null;
        }
        for (int i = 0; i < array.size(); i++) {
            Product product = new Product();
            JSONObject temp = array.getJSONObject(i);
            if (temp.containsKey("id")) {
                product.setId(temp.getInteger("id"));
            }
            if (temp.containsKey("stockid")) {
                product.setStockid(temp.getInteger("stockid"));
            }
            if (temp.containsKey("stock")) {
                JSONObject stock = temp.getJSONObject("stock");
                if (stock.containsKey("s_date")) {
                    product.setS_date(stock.getString("s_date"));
                }
                if (stock.containsKey("time_no")) {
                    String[] time = stock.getString("time_no").split("-");
                    product.setStime(time[0]);
                    product.setEtime(time[1]);
                }
            }
            if(temp.containsKey("sname")){
                product.setSname(temp.getString("sname"));
            }
            list.add(product);
        }
        return list;
    }

    /**
     * 找到符合预定要求的场次
     * @param area
     * @return
     */
    public static Product getOrderId(List<Product> area) {

        List<Product> sevenList = new ArrayList<Product>();
        List<Product> eightList = new ArrayList<Product>();
        List<Product> otherList = new ArrayList<Product>();
        for(Product temp : area){
            if(temp.getStime().substring(0,2).equals("19") && !temp.getSname().equals("场地1") && !temp.getSname().equals("场地6")){
                sevenList.add(temp);
            }else if(temp.getStime().substring(0,2).equals("20") && !temp.getSname().equals("场地1") && !temp.getSname().equals("场地6")){
                eightList.add(temp);
            }else{
                otherList.add(temp);
            }
        }

        //如果一张票也没抢到，而且没有7点场，就随便抢
        if(status == 0 && sevenList.size() == 0){
            status = 3;
        }
        if(status == 1 && eightList.size() == 0){
            status = 4;
        }

        switch(status){
            case 0:
                for (Product temp : sevenList){
                    if(null != temp){
                        status = 1;
                        return temp;
                    }
                }
                break;
            case 1:
                for (Product temp : eightList){
                    if(null != temp){
                        status = 2;
                        return temp;
                    }
                }
                break;
            case 2:
                logger.debug("已经抢到了需要的票，即将退出抢票");
                break;
            case 3:
                logger.debug("没有抢到7点的票，即将随机抢票");
                break;
            case 4:
                logger.debug("只抢到7点的票，但是没有8点的，即将随机抢票");
                break;
            default:
                logger.error("未知状态");
                break;
        }
        return area.get(area.size() - 1);
    }

    /**
     * 登录的函数
     * @param id
     * @param loginid
     * @param password
     * @param driver
     * @throws Exception
     */
    public static void login(int id, String loginid, String password, WebDriver driver) throws Exception{
        String url = "http://gym.sysu.edu.cn/product/show.html?id=" + id;
        driver.get(url);
        driver.findElement(By.linkText("登录")).click();
        driver.findElement(By.id("username")).sendKeys(loginid);
        driver.findElement(By.id("password")).sendKeys(password);
        while (driver.getTitle().equals("Login - 中央身份验证服务(CAS)")) {
            logger.info("请输入验证码！");
            Thread.sleep(1000);
        }
        logger.info("登录完成！开始自动占场。");
    }

    /**
     * 下订单的函数
     * @param date
     * @param id
     * @param driver
     * @throws Exception
     */
    public static void order(String date, int id, WebDriver driver) throws Exception {
        String url = "http://gym.sysu.edu.cn/product/show.html?id=" + id;
        driver.get(url);
        List<Product> area = getOkArea(date, id);
        int count = 1;
        while (null == area) {
            logger.debug("第" + count + "次查询空场!");
            area = getOkArea(date, id);
            count++;
            Thread.sleep(100);
        }
        Product stock = getOrderId(area);
        logger.debug("将要抢：" + stock);
        int stockid = stock.getStockid();
        driver.navigate().refresh();
        driver.findElement(By.xpath("//li[@data='" + date + "']")).click();
        String regex = "//span[@data-stockid='" + stockid + "']";
        driver.findElement(By.xpath(regex)).click();
        driver.findElement(By.id("reserve")).click();
        driver.findElement(By.id("reserve")).click();
    }

}



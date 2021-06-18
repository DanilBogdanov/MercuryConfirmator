package main.java.Mercury.model;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Данил on 04.04.2019.
 */
public class MercuryParser implements Runnable {
    private WebDriver webDriver;
    Company company;
    public static boolean isInterrupted = false;
    private boolean visible;
    //логин для входа в меркурий
    static private String logMercury;
    //пароль меркурия
    static private String pasMercury;
    public final static String URL_START_PAGE = "https://mercury.vetrf.ru/";
    static int countOfDone = 0;
    static int maxWeight = 0;
    static Date maxDate = null;

    {
        try (InputStream is = Files.newInputStream(Paths.get("/main/resources/prop.properties"))) {
            Properties properties = new Properties();
            properties.load(is);
            logMercury = properties.getProperty("logMercury");
            pasMercury = properties.getProperty("pasMercury");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BlockingQueue<MercuryItem> mercuryItemQueue = new LinkedBlockingQueue<>();

    public MercuryParser(boolean visible) {
        this.visible = visible;
    }

    public MercuryParser(boolean visible, Company company) {
        this.visible = visible;
        this.company = company;
    }

    public   WebDriver getWebDriver() {
        if (webDriver == null) {
            if (!visible) {
                File chromeDriverFile = new File("chromedriver.exe");
                //File mozilaDriverFile = new File("geckodriver.exe");
                String chromeDriverPath = chromeDriverFile.getAbsolutePath();
                //String mozilaDriverPath = mozilaDriverFile.getAbsolutePath();


                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                //System.setProperty("webdriver.firefox.driver", mozilaDriverPath);
                ChromeOptions chromeOptions = new ChromeOptions();
                //FirefoxOptions firefoxOptions = new FirefoxOptions();
                chromeOptions.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
                //firefoxOptions.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors", "--silent");
                webDriver = new ChromeDriver(chromeOptions);
                //webDriver = new FirefoxDriver(firefoxOptions);
            } else {
                webDriver = new ChromeDriver();
            }
            webDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);//ожидание n секунд до проброса exception
            webDriver.manage().window().setSize(new Dimension(700, 700));

            getWebDriver().get(URL_START_PAGE); //start on page https://mercury.vetrf.ru/
            getWebDriver().findElement(By.partialLinkText("Меркурий.ХС")).click(); //the choice хозяйствующий субъект
            authorization();
        }
        return webDriver;
    }


    private int getMercuryItems(Company company) //список ветдокументов по всем страницам
    {
        int count = 0;
        try {
            goToListOfVetDocs(company);//переход на страницу с ветдоками

            int countOfPage = 0;
            boolean isNextPage = true;
            System.out.println("start search and getting items");
            while (isNextPage && !isInterrupted) {
                System.out.println(++countOfPage);
                System.out.println("====================================");

                count += getMercuryItemsFromPage(getWebDriver().findElement(By.className("emplist")));//emplist класс на странице с ветсправками


                //проверка на наличие кнопки следующая страница
                WebElement pageNavigator = getWebDriver().findElement(By.cssSelector("#pageNavBlock > div.pagenav.pagenavContinuous"));//содержимое блока навигации по страницам
                if (pageNavigator.getText().contains("Следующая")) {//если есть кнопка "следующая" кликаем по ней
                    pageNavigator.findElement(By.linkText("Следующая")).click();
                } else {//если нет прерываем цикл
                    isNextPage = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private int getMercuryItemsFromPage(WebElement webElement)// список ветдокументов с одной страницы в очередь
    {
        int count = 0;
        try {
            //позиции с именами классов first and second
            List<WebElement> items = webElement.findElements(By.className("first"));
            items.addAll(webElement.findElements(By.className("second")));

            for (WebElement item : items) {
                MercuryItem mercuryItem = new MercuryItem();


                //проход по значениям "rtValue"
                List<WebElement> values = item.findElements(By.className("rtValue"));

                mercuryItem.id = Long.parseLong(values.get(0).getText());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
                mercuryItem.date = simpleDateFormat.parse(values.get(1).getText());
                mercuryItem.nameOfProduct = values.get(2).getText();
                mercuryItem.volume = Double.parseDouble(values.get(3).getText().replace(" кг", "").replace(",", "."));
                mercuryItem.productionDate = values.get(4).getText();//дата производства как текст
                mercuryItem.companySender = values.get(5).getText();

                //ссылка на страницу
                mercuryItem.url = item.findElement(By.cssSelector("a")).getAttribute("href");
                mercuryItemQueue.add(mercuryItem);
                count++;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private void authorization() //подстановка логина, пароля и клик по кнопке
    {
        getWebDriver().findElement(By.id("username")).sendKeys(logMercury);  //authorization, input login
        getWebDriver().findElement(By.id("password")).sendKeys(pasMercury);//authorization, input password
        getWebDriver().findElement(By.name("_eventId_proceed")).click();//authorization, click logIn
//        JOptionPane.showInputDialog(,
//                new String[] {"Неверно введен пароль!",
//                        "Повторите пароль :"},
//                "Авторизация",
//                JOptionPane.WARNING_MESSAGE);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void updateCountOfCompany() {
        goToChoiceCompany();

        //Выбор обслуживаемого предприятия ->
        for (Company company : Company.values()) {
            int count = 0;
            List<WebElement> list = getWebDriver().findElements(By.cssSelector("#body > form > table > tbody > tr:nth-child(" +
                    company.getNumberOfRadioButton() + ") > td.value > label > a"));
            if (list.size() > 0) {
                try {
                    count = Integer.parseInt(list.get(0).getText());
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Ощибка получения количества " + company.getName());
                    count = -1;
                }
            }
            company.setCount(count);
        }

    }

    //todo private
    public void goToListOfVetDocs(Company company) //переход на страницу с ветдокументами
    {
        try {
            goToChoiceCompany();
            //Выбор обслуживаемого предприятия ->
            getWebDriver().findElement(By.cssSelector("#body > form > table > tbody > tr:nth-child(" + company.getNumberOfRadioButton() + ") > td.value > label > input[type=\"radio\"]")).click();
            getWebDriver().findElement(By.className("positive")).click(); // click button "Выбрать"
            getWebDriver().findElement(By.cssSelector("#main-menu > ul > li:nth-child(7)")).click(); //click "Ветеринарные документы"
            Thread.sleep(500); //пауза, иначе вылетает exception
            //click "Оформленные"
            getWebDriver().findElement(By.xpath("//*[@id=\"body\"]/table/tbody/tr/td[3]/ul/li/ul/li[1]/a")).click();
            getWebDriver().findElement(By.cssSelector("#pageNavBlock > div:nth-child(3) > select")).click();// click at "поазывать по .."
            getWebDriver().findElement(By.cssSelector("#pageNavBlock > div:nth-child(3) > select > option:nth-child(6)")).click(); //the choice at 100
        } catch (Exception e) {
            System.out.println("Trouble on \"goToListOfVetDocs\"");
            e.printStackTrace();
        }
    }

    private void goToChoiceCompany() {
        getWebDriver().get(URL_START_PAGE); //start on page https://mercury.vetrf.ru/
        getWebDriver().findElement(By.partialLinkText("Меркурий.ХС")).click(); //the choice хозяйствующий субъект
        //Выбор обслуживаемого хозяйствующего субъекта
        getWebDriver().findElement(By.cssSelector("#body > form > table > tbody > tr:nth-child(2) > td.value > label > input[type=\"radio\"]")).click();
        getWebDriver().findElement(By.className("positive")).click(); // click button "Выбрать"
    }

    private void clickItem(MercuryItem item, int maxWeight, Date maxDate) {
        System.out.println("clicked");
        if (item.volume < maxWeight && item.date.before(maxDate)) {
            getWebDriver().get(item.url);
            getWebDriver().findElement(By.className("positive")).click();
            getWebDriver().findElement(By.className("positive")).click();
        }
    }

    public void mainLoop(int maxWeight, Date maxDate, boolean visible, Company company) {
        MercuryParser.maxWeight = maxWeight;
        MercuryParser.maxDate = maxDate;
        MercuryParser.isInterrupted = false;
        mercuryItemQueue.clear();


        MercuryParser runnable1 = new MercuryParser(visible, company);
        MercuryParser runnable2 = new MercuryParser(visible, company);
        Thread thread1 = new Thread(runnable1);
        Thread thread2 = new Thread(runnable2);
        thread1.setDaemon(true);
        thread2.setDaemon(true);
        thread1.start();
        thread2.start();

        int countOfItemsLoaded = getMercuryItems(company);


        System.out.println("Всего загружено " + countOfItemsLoaded);
    }

    public void run() {
        System.out.println("start thread");
        if (company != null) {
            goToListOfVetDocs(company);
            while (!MercuryParser.isInterrupted) {
                try {
                    MercuryItem item = mercuryItemQueue.take();
                    clickItem(item, maxWeight, maxDate);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.err.println("company didn't initialize");
        }
        System.out.println("stop thread");
        webDriver.close();
    }
}

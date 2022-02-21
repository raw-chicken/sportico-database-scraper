import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;

public class Scraper {
    private static WebDriver driver;

    private static Select type;
    private static Select category;
    private static Select sport;
    private static Select year;

    public static void main(String[] args) {

        // Create webdriver
        driver = createDriver();

        // Navigate to site
        driver.get("https://d3data.sportico.com/NCAADatabase/index.html");

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));

        getValues();

        driver.quit();

    }

    private static void getValues() {
        driver.findElement(By.id("viewAllSchools")).click();

        String[] categories = {"Athletic Student Aid", "Coaching Salaries, Benefits and Bonuses paid by the University and Related Entities",
                "Support Staff/ Administrative Compensation, Benefits and Bonuses paid by the University and Related Entities",
                "Sports Equipment, Uniforms and Supplies",
                "Athletic Facilities Debt Service, Leases and Rental Fee"};
//        String[] categories = {"Support Staff/ Administrative Compensation, Benefits and Bonuses paid by the University and Related Entities"};

        String[] years = {"2018", "2019", "2020", "2021"};


        category = new Select(driver.findElement(By.id("category")));
        type = new Select(driver.findElement(By.id("type")));
        sport = new Select(driver.findElement(By.id("sport")));
        year = new Select(driver.findElement(By.id("year")));

        // Total expenses
        for (String y : years) {
            System.out.printf("Parsing: School, Expenses, %s\n", y);
            StringBuilder sb = new StringBuilder();
            sb.append("School, Expenses\n");
            selectCategories("Individual Sports", "Expenses", "Men's Basketball", y);
            parseValues(sb, "Expenses", y);
        }

        // Expense sub-categories
        for (String c : categories) {
            for (String y : years) {
                System.out.printf("Parsing: %s, %s\n", c, y);
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("School, %s\n", c.replaceAll("[^a-zA-Z0-9]", "")));
                selectCategories("Expenses", c, "1", y);
                parseValues(sb, c, y);
            }
        }


    }

    // Parse values
    private static void parseValues(StringBuilder sb, String n, String y) {
        WebElement table = driver.findElement(By.xpath("/html/body/div[2]/table/tbody"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));

        for (WebElement row : rows) {
            List<WebElement> cols = row.findElements(By.tagName("td"));
            String[] out = new String[3];
            int i = 0;
            for (WebElement col : cols) {
                out[i++] = col.getText();
            }
            sb.append(String.format("%s, %s\n", out[1].replaceAll("[^a-zA-Z0-9]", ""),
                    out[2].replaceAll("[^a-zA-Z0-9]", "")));
//            System.out.printf("Rank: %s, School: %s, Value: %s\n", out[0], out[1], out[2]);
        }


        // This is ugly. Idc. I'm too lazy at this point
        if (n.equals("Support Staff/ Administrative Compensation, Benefits and Bonuses paid by the University and Related Entities")) {
            n = "Support staff";
        } else if (n.equals("Athletic Student Aid")) {
            n = "Student aid";
        } else if (n.equals("Coaching Salaries, Benefits and Bonuses paid by the University and Related Entities")) {
            n = "Coaching salaries";
        } else if (n.equals("Sports Equipment, Uniforms and Supplies")) {
            n = "Sports equipment";
        } else if (n.equals("Athletic Facilities Debt Service, Leases and Rental Fee")) {
            n = "Athletic facilities";
        }

        String file = String.format("%s_%s.csv", y, n);

        try (PrintWriter pw = new PrintWriter(file)) {
            pw.write(sb.toString());
            pw.flush();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

    }

    // Selects sport / year
    private static void selectCategories(String t, String c, String s, String y) {
        type.selectByValue(t);
        category.selectByValue(c);
        sport.selectByValue(s);
        year.selectByValue(y);
        driver.findElement(By.id("toggle")).click();
    }


    // Sets properties and creates webdriver
    private static WebDriver createDriver() {
        // Set properties
        System.setProperty("webdriver.gecko.driver", "/git/Basketball_Scraper/src/main/resources/geckodriver.exe");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "null");

        FirefoxOptions options = new FirefoxOptions();
        options.setHeadless(true);

        return new FirefoxDriver(options);
    }

}

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WebHeandlessMagMon {

    public static Boolean autorise(Integer NumberMagMonList, JTextArea LogOut) throws IOException {
        Boolean result = false;
        try {
            MagMonRec MagMon = MainForm.MagMonList.get(NumberMagMonList);
            final WebClient webClient = new WebClient();
            //Загружаем нужную страницу
            final HtmlPage page1 = webClient.getPage("http://" + MagMon.IP + ":" + MagMon.Port);
            // Выбираем нужную форму,
            // находим кнопку отправки и поле, которое нужно заполнить.
            final HtmlForm form = page1.getFormByName("login");

            final HtmlTextInput textLogin = form.getInputByName("UserName");
            final HtmlPasswordInput textPass = form.getInputByName("PassWord");
            final HtmlSubmitInput button = form.getInputByValue("Submit");// .getInputByName("submitbutton");

            // Записывает в найденное поле нужное значение.
            textLogin.setValueAttribute(MagMon.Login);
            textPass.setValueAttribute(MagMon.Pass);
            // Теперь «кликаем» на кнопку и переходим на новую страницу.
            final HtmlPage page2 = button.click();
            webClient.close();
            result = true;
        }catch (Exception e){
            result = false;
        }
        return result;
    }

    public static Boolean getData(Integer NumberMagMonList, JTextArea LogOut) throws IOException {
        Boolean result = false;
        MagMonRec MagMon = MainForm.MagMonList.get(NumberMagMonList);
        final WebClient webClient = new WebClient();
        HtmlPage page3 = webClient.getPage("http://"+MagMon.IP+":"+MagMon.Port+"/coldhead.html");
        HtmlForm form2 = page3.getFormByName("curVal");

        HtmlTextInput textBUF = form2.getInputByName("Ch15");
        MainForm.MagMonList.get(NumberMagMonList).setHePress(textBUF.getText());
        String hePress = textBUF.getText();
        MainForm.MagMonList.get(NumberMagMonList).setStatus("ok");
        String status = "ok";
        //LogOut.append(" He getData Pressure = "+textBUF+"\n");
        Date date = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("HH:mm:ss");

        long currentTime = System.currentTimeMillis()/1000;
        //System.out.println( " He Pressure = "+textBUF +" "+ formatForDateNow.format(date));

        MainForm.MagMonList.get(NumberMagMonList).setLastTime(formatForDateNow.format(date));

        textBUF = form2.getInputByName("Ch1");
        MainForm.MagMonList.get(NumberMagMonList).setHeLevel(textBUF.getText());
        String heLevel = textBUF.getText();

        page3 = webClient.getPage("http://"+MagMon.IP+":"+MagMon.Port+"/cur_a_vals.html");
        form2 = page3.getFormByName("curVal");

        textBUF = form2.getInputByName("Ch16");
        MainForm.MagMonList.get(NumberMagMonList).setWaterFlow1(textBUF.getText());
        String wf1 = textBUF.getText();

        textBUF = form2.getInputByName("Ch17");
        MainForm.MagMonList.get(NumberMagMonList).setWaterTemp1(textBUF.getText());
        String wt1 = textBUF.getText();

        textBUF = form2.getInputByName("Ch25");
        MainForm.MagMonList.get(NumberMagMonList).setWaterFlow2(textBUF.getText());
        String wf2 = textBUF.getText();

        textBUF = form2.getInputByName("Ch26");
        MainForm.MagMonList.get(NumberMagMonList).setWaterTemp2(textBUF.getText());
        String wt2 = textBUF.getText();

        //LogOut.append(" Ch26 = "+textBUF.getText()+"\n");
        page3 = webClient.getPage("http://"+MagMon.IP+":"+MagMon.Port+"/alarms.html");
        WebResponse response = page3.getWebResponse();
        String content = response.getContentAsString();
        Document doc = (Document) Jsoup.parseBodyFragment(content);
        Elements fullHtml = doc.getElementsByTag("pre");
        //System.out.println(fullHtml.toString());
        ArrayList<String> bufList = ErrParse(fullHtml.toString());
        String buf;
        if(bufList.size()<1){
            buf = "OK";
        }
        else{
            buf = bufList.size()+" Error";
        }
        MainForm.MagMonList.get(NumberMagMonList).setStatus(buf);
        status = buf;
        MainForm.MagMonList.get(NumberMagMonList).setErrors(bufList);
        String errors = String.join("|", bufList);

        try {
            Connection connection = DatabaseManager.getInstance().openDatabase();
            Statement statement = connection.createStatement();
            String mMName = MainForm.MagMonList.get(NumberMagMonList).getName();
            statement.executeUpdate("INSERT INTO "+DatabaseManager.TABLE_NAME+
                    " (name, hepress, heperc, wt1, wt2, wf1, wf2, status, error, dateupdate) VALUES ("
                    +"\""+mMName+"\", \""+hePress+"\", \""+heLevel+"\", \""+wt1+"\", \""+wt2+"\", \""+wf1+"\", \""+wf2+"\" " +
                    " \""+status+"\", \""+errors+"\", "+currentTime+")");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (DatabaseManager.getInstance() != null)
                DatabaseManager.getInstance().closeDatabase();
        }
        webClient.close();
        return result;
    }

    public static ArrayList<String> ErrParse(String sourcetext){
        ArrayList<String> result = null;
        ArrayList<String> ListStr = new ArrayList<String>();
        ArrayList<String> ListBuf = new ArrayList<String>();
        for (String retval : sourcetext.split("\\n")) {
            ListStr.add(retval);
        }
        for(int i=0; i<=ListStr.size()-1;i++){
            if(ListStr.get(i).contains("<")|(ListStr.get(i).length()<2)|(ListStr.get(i).contains("-"))){

            }
            else {
                ListBuf.add(ListStr.get(i));
            }
        }
        return ListBuf;
    }
}

import uz.alexander.utils.Logger;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

public class MainTask extends TimerTask {

    JTextArea LogOut;

    public MainTask(JTextArea LogOut){
        this.LogOut = LogOut;
    }

    @Override
    public void run() {
        Object[][] array = new String[MainForm.MagMonList.size()][9];
        Date date = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat();
        Date currentDate = new Date();
        for(int i=0; i<=MainForm.MagMonList.size()-1;i++) {
            try {
                boolean autoriseOk = WebHeandlessMagMon.autorise(i, LogOut);
                Logger.handleMessage("Autorise", " Autorise " + autoriseOk);
                MainForm.tableModel.setRowCount(0);
                if(autoriseOk){
                    WebHeandlessMagMon.getData(i, LogOut);
                    //MainForm.tableModel.setRowCount(0);
                    array[i][0] = MainForm.MagMonList.get(i).getName();
                    array[i][1] = MainForm.MagMonList.get(i).getHePress();
                    array[i][2] = MainForm.MagMonList.get(i).getHeLevel();
                    array[i][3] = MainForm.MagMonList.get(i).getWaterTemp1();
                    array[i][4] = MainForm.MagMonList.get(i).getWaterFlow1();
                    array[i][5] = MainForm.MagMonList.get(i).getWaterTemp2();
                    array[i][6] = MainForm.MagMonList.get(i).getWaterFlow2();
                    array[i][7] = MainForm.MagMonList.get(i).getStatus();
                    array[i][8] =formatForDateNow.format(date);
                    LogOut.append(formatForDateNow.format(currentDate)+
                        ":   Name: "+MainForm.MagMonList.get(i).getName()+ "; HePress: "+MainForm.MagMonList.get(i).getHePress()+
                        "; HeLevel: "+MainForm.MagMonList.get(i).getHeLevel()+ "; WaterTemp1: "+MainForm.MagMonList.get(i).getWaterTemp1()+
                        "; WaterFlow1: "+MainForm.MagMonList.get(i).getWaterFlow1()+
                                    "; WaterTemp2: "+MainForm.MagMonList.get(i).getWaterTemp2()+ "; WaterFlow2:"+MainForm.MagMonList.get(i).getWaterFlow2()+
                                "; Status: "+MainForm.MagMonList.get(i).getStatus()+ "\n");
                    MainForm.setDataAcquired();
                }else{
                    MainForm.tableModel.setRowCount(0);
                    array[i][0] = MainForm.MagMonList.get(i).getName();
                    array[i][7] = "No Connect";
                    array[i][8] = formatForDateNow.format(date);
                    ArrayList<String> bufList = new ArrayList<>();
                    bufList.add("No");
                    MainForm.MagMonList.get(i).setErrors(bufList);
                    LogOut.append(formatForDateNow.format(currentDate)+ ":   Name: "+MainForm.MagMonList.get(i).getName()+ "; Status: No Connect"+ "\n");
                    //e.printStackTrace();
                    Logger.handleMessage("Error", "Cannot connect to magmon");
                    if (MainForm.getLastDataAcquiredInterval() > 7200*1000) {//60*60*2 * 1000
                        if (!"".equals(MainForm.userPrefs.get(Constants.PREF_MODEM_SMSNUMBER,""))){
                            SmsSenderTask.addToQueue(MainForm.userPrefs.get(Constants.PREF_MODEM_SMSNUMBER,""), "No data for "+Math.round(MainForm.getLastDataAcquiredInterval()/1000D)+" seconds");
                        }
                        MainForm.setDataAcquired();
                    }
                }
            } catch (IOException e) {
//                MainForm.tableModel.setRowCount(0);
//                array[i][0] = MainForm.MagMonList.get(i).getName();
//                array[i][7] = "No Connect";
//                array[i][8] = formatForDateNow.format(date);
//                ArrayList<String> bufList = new ArrayList<>();
//                bufList.add("No");
//                MainForm.MagMonList.get(i).setErrors(bufList);
//                LogOut.append(formatForDateNow.format(currentDate)+ ":   Name: "+MainForm.MagMonList.get(i).getName()+ "; Status: No Connect"+ "\n");
//                //e.printStackTrace();
//                System.out.println("errorrs");
                Logger.handleException(e);
            }
        }
            for (Object[] objects : array) MainForm.tableModel.addRow(objects);
    }
}

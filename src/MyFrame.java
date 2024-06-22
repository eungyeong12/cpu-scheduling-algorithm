import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.data.time.SimpleTimePeriod;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

public class MyFrame extends JFrame {
    private String[] schedulingAlgorithm = {"선택", "FCFS(First-Come, First-Served)", "SJF(Shortest-Job-First)", "Priority", "RR(Round Robin)", "PRR(Priority Round Robin)"};
    private String[] timeSlice = {"선택", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private JTable inputTable, outputTable;
    private String[] inputCol = {"Process", "Arrive Time", "Burst Time", "Priority"};
    private String[] outputCol = {"Process", "Execution Time", "Waiting Time", "Turnaround Time", "Response Time"};
    private DefaultTableModel inputModel, outputModel;
    private String selectedAlgorithm = "";
    private String timeQuantum = "";
    private String selectedFilePath = "";
    private List<Process> pList;
    private List<Result> resultList;
    private JTextArea resultText1;
    private JTextArea resultText2;
    private JTextArea resultText3;
    private JTextArea resultText4;
    private JPanel centerPanel;
    private JPanel resultPanel;
    private TaskSeries series;

    public MyFrame() {
        setTitle("Scheduling Algorithm");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 상단
        JPanel topPanel = new JPanel(new FlowLayout());
        JLabel saLabel = new JLabel("Scheduling Algorithm");
        JComboBox<String> sa = new JComboBox<>(schedulingAlgorithm);
        topPanel.add(saLabel);
        topPanel.add(sa);

        JComboBox<String> ts = new JComboBox<>(timeSlice);
        JLabel tsLabel = new JLabel("Time Slice");


        centerPanel = new JPanel(new GridLayout(2, 1));
        JPanel tablePanel = new JPanel(new GridLayout(2, 1));
        // 입력 테이블
        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel inputLabel = new JLabel("입력");
        inputModel = new DefaultTableModel(inputCol, 0);
        inputTable = new JTable(inputModel);
        JScrollPane inputScrollPane = new JScrollPane(inputTable);
        inputPanel.add(inputLabel, BorderLayout.NORTH);
        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // 출력 테이블
        JPanel outputPanel = new JPanel(new BorderLayout());
        JLabel outputLabel = new JLabel("출력");
        outputModel = new DefaultTableModel(outputCol, 0);
        outputTable = new JTable(outputModel);
        JScrollPane outputScrollPane = new JScrollPane(outputTable);
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);

        // 결과
        resultPanel = new JPanel(new BorderLayout());
        JPanel resultPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel resultLabel1 = new JLabel("전체 실행시간: ");
        resultText1 = new JTextArea();
        resultText1.setEditable(false);
        resultPanel1.add(resultLabel1);
        resultPanel1.add(resultText1);

        JPanel resultPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel resultLabel2 = new JLabel("평균 대기시간: ");
        resultText2 = new JTextArea();
        resultText2.setEditable(false);
        JLabel resultLabel3 = new JLabel("    평균 turnaround time : ");
        resultText3 = new JTextArea();
        resultText3.setEditable(false);
        JLabel resultLabel4 = new JLabel("    평균 response time : ");
        resultText4 = new JTextArea();
        resultText4.setEditable(false);
        resultPanel2.add(resultLabel2);
        resultPanel2.add(resultText2);
        resultPanel2.add(resultLabel3);
        resultPanel2.add(resultText3);
        resultPanel2.add(resultLabel4);
        resultPanel2.add(resultText4);

        resultPanel.add(resultPanel1, BorderLayout.NORTH);
        resultPanel.add(resultPanel2, BorderLayout.CENTER);

        tablePanel.add(inputPanel);
        tablePanel.add(outputPanel);

        centerPanel.add(tablePanel);
        centerPanel.add(resultPanel);

        // 파일
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton fileChooserButton = new JButton("Choose File");
        JButton runButton = new JButton("Run");
        filePanel.add(fileChooserButton);
        filePanel.add(runButton);

        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();

        fileChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a file");
                fileChooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
                int result = fileChooser.showOpenDialog(MyFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                    DefaultTableModel model = (DefaultTableModel) inputTable.getModel();
                    model.setNumRows(0);
                    readFile(selectedFilePath);
                }
            }
        });

        sa.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedAlgorithm = (String) sa.getSelectedItem();
                    if (selectedAlgorithm.equals("RR(Round Robin)") || selectedAlgorithm.equals("PRR(Priority Round Robin)")) {
                        topPanel.add(tsLabel);
                        topPanel.add(ts);
                    } else {
                        topPanel.remove(tsLabel);
                        topPanel.remove(ts);
                    }
                    // 레이아웃을 다시 정렬
                    revalidate();
                    repaint();
                }
            }
        });

        ts.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED) {
                    timeQuantum = (String) ts.getSelectedItem();
                }
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(selectedFilePath.equals("")) {
                    JOptionPane.showMessageDialog(MyFrame.this, "파일을 선택해주세요");
                    return;
                }
                if (!selectedAlgorithm.equals("")) {
                    pList = new ArrayList<>();
                    for(int i=0; i<inputTable.getRowCount(); i++) {
                        Process p = new Process(Integer.parseInt((String) inputTable.getValueAt(i,0)), Integer.parseInt((String) inputTable.getValueAt(i,1)), Integer.parseInt((String) inputTable.getValueAt(i,2)), Integer.parseInt((String) inputTable.getValueAt(i,3)));
                        pList.add(p);
                    }
                    resultList = new ArrayList<>();
                    DefaultTableModel model = (DefaultTableModel) outputTable.getModel();
                    model.setNumRows(0);

                    switch (selectedAlgorithm) {
                        case "FCFS(First-Come, First-Served)":
                            runFCFS();
                            break;
                        case "SJF(Shortest-Job-First)":
                            runSJF();
                            break;
                        case "Priority":
                            runPriority();
                            break;
                        case "RR(Round Robin)":
                            if(timeQuantum.equals("")) {
                                JOptionPane.showMessageDialog(MyFrame.this, "Time Slice를 선택해주세요");
                                return;
                            }
                            runRR();
                            break;
                        case "PRR(Priority Round Robin)":
                            if(timeQuantum.equals("")) {
                                JOptionPane.showMessageDialog(MyFrame.this, "Time Slice를 선택해주세요");
                                return;
                            }
                            runPRR();
                            break;
                    }
                } else {
                    JOptionPane.showMessageDialog(MyFrame.this, "Scheduling Algorithm을 선택해주세요");
                }
            }
        });

        add(topPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(filePanel, BorderLayout.SOUTH);

        setSize(800, 800);
        setVisible(true);
    }

    private void readFile(String filePath) {
        inputModel.setRowCount(0);
        outputModel.setRowCount(0);

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, " ");
                st.nextToken();
                String[] rowData = new String[4];
                for (int i = 0; i < 4; i++) {
                    rowData[i] = st.nextToken();
                }
                inputModel.addRow(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runFCFS() {
        FCFS fcfs = new FCFS();
        resultList = fcfs.run(pList, resultList);

        series = new TaskSeries("cpu time");
        String[] rowData = new String[5];
        double waitingTime = 0.0;
        int burst = 0;
        double turnaroundTime = 0.0;
        double responseTime = 0.0;
        for(Result r : resultList) {
            rowData[0] = String.valueOf(r.processID);
            rowData[1] = String.valueOf(r.burstTime);
            rowData[2] = String.valueOf(r.waitingTime);
            rowData[3] = String.valueOf(r.turnaroundTime);
            rowData[4] = String.valueOf(r.responseTime);
            outputModel.addRow(rowData);

            series.add(new Task("p" + r.processID, new SimpleTimePeriod(burst, burst+ r.burstTime)));
            waitingTime += r.waitingTime;
            burst += r.burstTime;
            turnaroundTime += r.turnaroundTime;
            responseTime += r.responseTime;
        }

        resultText1.setText(String.valueOf(resultList.get(resultList.size()-1).startP + resultList.get(resultList.size()-1).burstTime));
        resultText2.setText(String.valueOf(String.format("%.2f", waitingTime / resultList.size())));
        resultText3.setText(String.valueOf(String.format("%.2f", turnaroundTime / resultList.size())));
        resultText4.setText(String.valueOf(String.format("%.2f", responseTime / resultList.size())));
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        dataset.add(series);
        makeChart("FCFS result", dataset, burst);
    }

    private void runSJF() {
        SJF sjf = new SJF();
        resultList = sjf.run(pList, resultList);

        series = new TaskSeries("cpu time");
        series.removeAll();
        String[] rowData = new String[5];
        double waitingTime = 0.0;
        int burst = 0;
        double turnaroundTime = 0.0;
        double responseTime = 0.0;

        for(Result r : resultList) {
            rowData[0] = String.valueOf(r.processID);
            rowData[1] = String.valueOf(r.burstTime);
            rowData[2] = String.valueOf(r.waitingTime);
            rowData[3] = String.valueOf(r.turnaroundTime);
            rowData[4] = String.valueOf(r.responseTime);
            outputModel.addRow(rowData);

            series.add(new Task("p" + r.processID, new SimpleTimePeriod(burst, burst+ r.burstTime)));
            waitingTime += r.waitingTime;
            burst += r.burstTime;
            turnaroundTime += r.turnaroundTime;
            responseTime += r.responseTime;
        }

        resultText1.setText(String.valueOf(resultList.get(resultList.size()-1).startP + resultList.get(resultList.size()-1).burstTime));
        resultText2.setText(String.valueOf(String.format("%.2f", waitingTime / resultList.size())));
        resultText3.setText(String.valueOf(String.format("%.2f", turnaroundTime / resultList.size())));
        resultText4.setText(String.valueOf(String.format("%.2f", responseTime / resultList.size())));
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        dataset.add(series);
        makeChart("SJF result", dataset, burst);
    }

    private void runPriority() {
        Priority priority = new Priority();
        resultList = priority.run(pList, resultList);

        series = new TaskSeries("cpu time");
        String[] rowData = new String[5];
        double waitingTime = 0.0;
        int burst = 0;
        double turnaroundTime = 0.0;
        double responseTime = 0.0;

        for(Result r : resultList) {
            rowData[0] = String.valueOf(r.processID);
            rowData[1] = String.valueOf(r.burstTime);
            rowData[2] = String.valueOf(r.waitingTime);
            rowData[3] = String.valueOf(r.turnaroundTime);
            rowData[4] = String.valueOf(r.responseTime);
            outputModel.addRow(rowData);

            series.add(new Task("p" + r.processID, new SimpleTimePeriod(burst, burst+ r.burstTime)));
            waitingTime += r.waitingTime;
            burst += r.burstTime;
            turnaroundTime += r.turnaroundTime;
            responseTime += r.responseTime;
        }

        resultText1.setText(String.valueOf(resultList.get(resultList.size()-1).startP + resultList.get(resultList.size()-1).burstTime));
        resultText2.setText(String.valueOf(String.format("%.2f", waitingTime / resultList.size())));
        resultText3.setText(String.valueOf(String.format("%.2f", turnaroundTime / resultList.size())));
        resultText4.setText(String.valueOf(String.format("%.2f", responseTime / resultList.size())));
        TaskSeriesCollection dataset = new TaskSeriesCollection();
        dataset.add(series);
        makeChart("Priority result", dataset, burst);
    }

    private void runRR() {
        RR rr = new RR(Integer.parseInt(timeQuantum));
        resultList = rr.run(pList, resultList);

        series = new TaskSeries("cpu time");
        String[] rowData = new String[5];
        Map<Integer, Integer> map = new HashMap<>();
        double waitingTime = 0.0;
        int burst = 0;
        Map<Integer, Integer> map2 = new HashMap<>();
        double turnaroundTime = 0.0;
        Map<Integer, Task> taskMap = new HashMap<>();
        double responseTime = 0.0;
        Map<Integer, Integer> map3 = new HashMap<>();

        for(Result r : resultList) {
            rowData[0] = String.valueOf(r.processID);
            rowData[1] = String.valueOf(r.burstTime);
            rowData[2] = String.valueOf(r.waitingTime);
            rowData[3] = String.valueOf(r.turnaroundTime);
            rowData[4] = String.valueOf(r.responseTime);
            outputModel.addRow(rowData);
            map.put(r.processID, r.waitingTime);
            map2.put(r.processID, r.turnaroundTime);
            if(map3.get(r.processID) == null)
                map3.put(r.processID, r.responseTime);
            else
                if(map3.get(r.processID) > r.responseTime)
                    map3.put(r.processID, r.responseTime);

            Task task = taskMap.get(r.processID);
            if (task == null) {
                task = new Task("p" + r.processID, new SimpleTimePeriod(burst, burst + r.burstTime));
                taskMap.put(r.processID, task);
            }
            Task subtask = new Task("p" + r.processID, new SimpleTimePeriod(burst, burst + r.burstTime));
            task.addSubtask(subtask);

            burst += r.burstTime;
        }

        Set<Map.Entry<Integer, Integer>> set = map.entrySet();
        for(Map.Entry<Integer, Integer> me : set)
            waitingTime += me.getValue();

        set = map2.entrySet();
        for(Map.Entry<Integer, Integer> me : set)
            turnaroundTime += me.getValue();

        set = map3.entrySet();
        for(Map.Entry<Integer, Integer> me : set)
            responseTime += me.getValue();

        resultText1.setText(String.valueOf(resultList.get(resultList.size()-1).startP + resultList.get(resultList.size()-1).burstTime));
        resultText2.setText(String.valueOf(String.format("%.2f", waitingTime / map.size())));
        resultText3.setText(String.valueOf(String.format("%.2f", turnaroundTime / map.size())));
        resultText4.setText(String.valueOf(String.format("%.2f", responseTime / map.size())));
        TaskSeriesCollection dataset = new TaskSeriesCollection();

        for (Task task : taskMap.values())
            series.add(task);

        dataset.add(series);
        makeChart("Round Robin result", dataset, burst);
    }

    private void runPRR() {
        PRR rr = new PRR(Integer.parseInt(timeQuantum));
        resultList = rr.run(pList, resultList);

        series = new TaskSeries("cpu time");

        String[] rowData = new String[5];
        Map<Integer, Integer> map = new HashMap<>();
        double waitingTime = 0.0;
        int burst = 0;

        Map<Integer, Integer> map2 = new HashMap<>();
        double turnaroundTime = 0.0;
        Map<Integer, Task> taskMap = new HashMap<>();

        double responseTime = 0.0;
        Map<Integer, Integer> map3 = new HashMap<>();

        for(Result r : resultList) {
            rowData[0] = String.valueOf(r.processID);
            rowData[1] = String.valueOf(r.burstTime);
            rowData[2] = String.valueOf(r.waitingTime);
            rowData[3] = String.valueOf(r.turnaroundTime);
            rowData[4] = String.valueOf(r.responseTime);
            outputModel.addRow(rowData);
            map.put(r.processID, r.waitingTime);
            map2.put(r.processID, r.turnaroundTime);
            if(map3.get(r.processID) == null)
                map3.put(r.processID, r.responseTime);
            else
            if(map3.get(r.processID) > r.responseTime)
                map3.put(r.processID, r.responseTime);

            Task task = taskMap.get(r.processID);
            if (task == null) {
                task = new Task("p" + r.processID, new SimpleTimePeriod(burst, burst + r.burstTime));
                taskMap.put(r.processID, task);
            }
            Task subtask = new Task("p" + r.processID, new SimpleTimePeriod(burst, burst + r.burstTime));
            task.addSubtask(subtask);

            burst += r.burstTime;
        }

        Set<Map.Entry<Integer, Integer>> set = map.entrySet();
        for(Map.Entry<Integer, Integer> me : set)
            waitingTime += me.getValue();

        set = map2.entrySet();
        for(Map.Entry<Integer, Integer> me : set)
            turnaroundTime += me.getValue();

        set = map3.entrySet();
        for(Map.Entry<Integer, Integer> me : set)
            responseTime += me.getValue();

        resultText1.setText(String.valueOf(resultList.get(resultList.size()-1).startP + resultList.get(resultList.size()-1).burstTime));
        resultText2.setText(String.valueOf(String.format("%.2f", waitingTime / map.size())));
        resultText3.setText(String.valueOf(String.format("%.2f", turnaroundTime / map.size())));
        resultText4.setText(String.valueOf(String.format("%.2f", responseTime / map.size())));
        TaskSeriesCollection dataset = new TaskSeriesCollection();

        for (Task task : taskMap.values())
            series.add(task);

        dataset.add(series);
        makeChart("Priority Round Robin result", dataset, burst);
    }

    private void makeChart(String title, TaskSeriesCollection dataset, int burst) {
        JFreeChart chart = ChartFactory.createGanttChart(title, "process", "time", dataset);
        CategoryPlot plot = chart.getCategoryPlot();
        DateAxis axis = (DateAxis) plot.getRangeAxis();
        axis.setMaximumDate(new Date(burst));
        axis.setDateFormatOverride(new SimpleDateFormat("SS"));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 290));

        resultPanel.add(chartPanel, BorderLayout.SOUTH);
        centerPanel.add(resultPanel);
        chartPanel.revalidate();
    }

    public static void main(String[] args) {
        new MyFrame();
    }
}

import util.FileHelper;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class CodRepDatasetMain {
    static String datasetPath = "/home/huyenhuyen/Desktop/data-bugfixes/CodRep-competition/Datasets";
    static String datasetFormat = "/home/huyenhuyen/Desktop/data-bugfixes/CodRepFormat";
    static String out = "/home/huyenhuyen/obseve/out/codrep";

    public static void main(String[] args) throws Exception {
        if (new File(datasetFormat).listFiles().length == 0) {
            mainFormatData();
        } else {
            String pathOld = "";
            String pathNew = "";

            File[] files = new File(datasetFormat).listFiles();
            HDRepairDatatasetMain hdRepairDatatasetMain = new HDRepairDatatasetMain(out);
            for (File file : files) {
                try {
                    pathOld = file.getAbsolutePath() + File.separator + "old";
                    pathNew = file.getAbsolutePath() + File.separator + "fix";
                    File fix = Objects.requireNonNull(new File(file.getAbsolutePath() + File.separator + "fix").listFiles())[0];
                    File old = Objects.requireNonNull(new File(file.getAbsolutePath() + File.separator + "old").listFiles())[0];
                    hdRepairDatatasetMain.getDiff(old, fix, fix.getName().replace(".txt", ""));
                } catch (Exception e) {
                    System.out.println("===================================");
//                e.printStackTrace();
                    System.out.println(pathOld);
                    System.out.println(pathNew);
                }
            }
            hdRepairDatatasetMain.writeToXlSX(hdRepairDatatasetMain.contexts, "CodRed");
            FileHelper.outputToFile(new File("out/codrep/bugs.txt"), hdRepairDatatasetMain.list, false);
            System.out.println(hdRepairDatatasetMain.counts.toString());


        }
    }

//    public static void main(String[] args) throws Exception {
//                   HDRepairDatatasetMain hdRepairDatatasetMain = new HDRepairDatatasetMain(out);
//        File old = Objects.requireNonNull(new File("/home/huyenhuyen/Desktop/data-bugfixes/CodRepFormat/Dataset1_2913/old/2913.txt"));
//        File fix = Objects.requireNonNull(new File("/home/huyenhuyen/Desktop/data-bugfixes/CodRepFormat/Dataset1_2913/fix/2913.txt"));
//        hdRepairDatatasetMain.getDiff(old, fix, fix.getName());
//        System.out.println(hdRepairDatatasetMain.contexts);
//    }

    public static void mainFormatData() {
        for (int i = 0; i < 5; i++) {
            String datasetname = "Dataset" + (i + 1);
            datasetPath += File.separator + datasetname;
            File task = new File(datasetPath + File.separator + "Tasks");
            File solution = new File(datasetPath + File.separator + "Solutions");
            File[] taskChild = task.listFiles();
            File[] solutionChild = solution.listFiles();
            if (taskChild != null && solutionChild != null) {
                for (int c = 0; c < taskChild.length; c++) {
                    File taskBug = taskChild[c];
                    File solutionBug = solutionChild[c];
                    if (taskBug.getName().equals(solutionBug.getName())) {
                        List<String> content = FileHelper.readDataAsList(taskBug.getAbsolutePath());
                        List<String> lineString = FileHelper.readDataAsList(solutionBug.getAbsolutePath());
                        if (lineString.size() == 1) {
                            format(content, Integer.parseInt(lineString.get(0)), datasetname, solutionBug.getName());
                        } else if (lineString.size() > 1) {
                            System.out.println("SIZE > 1");
                            return;
                        }
                    }
                }
            }
        }
    }


    private static void format(List<String> content, int line, String dtName, String bugFileName) {
        String buggyLine = content.get(0);
        String bugString = "";
        String fixString = "";
        for (int i = 2; i < content.size(); i++) {
            if (i == line + 1) {
                fixString += buggyLine + "\n";
            } else {
                fixString += content.get(i) + "\n";
            }
            bugString += content.get(i) + "\n";
        }
        File fileBug = new File(datasetFormat + File.separator
                + dtName + "_" + bugFileName.replace(".txt", "")
                + File.separator + "old" + File.separator + bugFileName);
        fileBug.getParentFile().mkdirs();
        FileHelper.createFile(fileBug, bugString);

        File fileFix = new File(datasetFormat + File.separator
                + dtName + "_" + bugFileName.replace(".txt", "")
                + File.separator + "fix" + File.separator + bugFileName);
        fileFix.getParentFile().mkdirs();
        FileHelper.createFile(fileFix, fixString);
    }
}


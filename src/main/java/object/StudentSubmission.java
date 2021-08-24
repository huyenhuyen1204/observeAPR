package object;

import java.util.ArrayList;
import java.util.List;

public class StudentSubmission {
    private String submission100;
    private List<String> submissions; //submission != 100 & != CE

    public StudentSubmission() {
        submissions = new ArrayList<>();
    }

    public String getSubmission100() {
        return submission100;
    }

    public void setSubmission100(String submission100) {
        this.submission100 = submission100;
    }

    public List<String> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<String> submissions) {
        this.submissions = submissions;
    }

    public void addSubmission(String submission) {
        submissions.add(submission);
    }
}

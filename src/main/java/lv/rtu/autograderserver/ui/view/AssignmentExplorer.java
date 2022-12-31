package lv.rtu.autograderserver.ui.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import de.f0rce.ace.AceEditor;
import de.f0rce.ace.enums.AceMode;
import lv.rtu.autograderserver.model.Participant;
import lv.rtu.autograderserver.model.Submission;
import lv.rtu.autograderserver.ui.component.NotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

public class AssignmentExplorer extends VerticalLayout {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Participant participantData;
    private int currentSubmissionIdx = 0;
    private Submission currentSubmission;

    private final AceEditor codeEditor = new AceEditor();
    private final H2 problemTitle = new H2();
    private final Label problemNumberIndicator = new Label();
    private final Div problemDescription = new Div();

    private Consumer<Participant> callBack;


    public AssignmentExplorer(Participant participant) {
        this.participantData = participant;
        initContent();
        switchSubmission(currentSubmissionIdx);
    }

    private void initContent() {
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setSizeFull();

        codeEditor.setHeightFull();
        codeEditor.setWidth(50, Unit.PERCENTAGE);
        codeEditor.addValueChangeListener(event -> {
            if (currentSubmission != null) {
                currentSubmission.setSolutionContent(event.getValue());
            }
        });

        codeEditor.addBlurListener(event -> {
            NotificationHelper.displaySuccess("TEST");
            if (callBack != null) {
                callBack.accept(participantData);
            }
        });

        VerticalLayout descriptionBlock = new VerticalLayout();
        descriptionBlock.setWidth(50, Unit.PERCENTAGE);
        descriptionBlock.setHeightFull();

        HorizontalLayout problemSwitcher = createProblemSwitcher();

        descriptionBlock.add(problemSwitcher, problemTitle, problemDescription);

        contentLayout.add(codeEditor, descriptionBlock);

        add(contentLayout);
    }

    public void registerCallback(@NotNull Consumer<Participant> cb) {
        this.callBack = cb;
    }

    private HorizontalLayout createProblemSwitcher() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();

        Button prev = new Button(getTranslation("assignment_view_btn_previous"));
        prev.setIcon(VaadinIcon.CHEVRON_LEFT_SMALL.create());
        prev.addClickListener(event -> switchSubmission(currentSubmissionIdx - 1));


        Button next = new Button(getTranslation("assignment_view_btn_next"));
        next.setIcon(VaadinIcon.CHEVRON_RIGHT_SMALL.create());
        next.setIconAfterText(true);
        next.addClickListener(event -> switchSubmission(currentSubmissionIdx + 1));

        layout.add(prev, problemNumberIndicator, next);
        layout.setJustifyContentMode(JustifyContentMode.CENTER);
        layout.setAlignItems(Alignment.CENTER);

        return layout;
    }
    
    private void switchSubmission(int idx) {
        long totalNumberOfProblems = participantData.getSubmissions().size();
        if (idx < 0 || idx >= totalNumberOfProblems) {
            return;
        }

        currentSubmissionIdx = idx;
        problemNumberIndicator.setText(getTranslation("assignment_view_problem_num_indicator", (idx + 1), totalNumberOfProblems));
        currentSubmission = participantData.getSubmissions().get(idx);
        updateSubmissionContent();
    }

    private void updateSubmissionContent() {
        switch (currentSubmission.getProblem().getSandboxType()) {
            case JAVA11:
                codeEditor.setMode(AceMode.java);
                break;
            case PYTHON:
                codeEditor.setMode(AceMode.python);
                break;
        }

        codeEditor.setValue(currentSubmission.getSolutionContent());
        problemTitle.setText(currentSubmission.getProblem().getTitle());

        problemDescription.removeAll();
        problemDescription.add(new Html(String.format("<div>%s</div>", currentSubmission.getProblem().getDescription())));
    }
}

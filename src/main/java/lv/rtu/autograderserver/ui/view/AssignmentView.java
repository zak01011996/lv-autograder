package lv.rtu.autograderserver.ui.view;

import com.flowingcode.vaadin.addons.simpletimer.SimpleTimer;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lv.rtu.autograderserver.model.Participant;
import lv.rtu.autograderserver.model.Publication;
import lv.rtu.autograderserver.model.Task;
import lv.rtu.autograderserver.service.ParticipantService;
import lv.rtu.autograderserver.service.PublicationService;
import lv.rtu.autograderserver.ui.component.NotificationHelper;
import lv.rtu.autograderserver.ui.component.form.ParticipantRegistrationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@AnonymousAllowed
@Route("/assignment/:publicationId")
public class AssignmentView extends AppLayout implements BeforeEnterObserver, HasDynamicTitle {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Publication publication;
    private Participant participantData;

    private final PublicationService publicationService;
    private final ParticipantService participantService;

    private final VerticalLayout mainContent = new VerticalLayout();

    public AssignmentView(@NotNull PublicationService publicationService, @NotNull ParticipantService participantService) {
        this.publicationService = publicationService;
        this.participantService = participantService;

        getElement().getStyle().set("height", "100%");

        mainContent.setSizeFull();
        setContent(mainContent);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        try {
            String publicationId = event.getRouteParameters().get("publicationId").orElseThrow(() ->
                    new RuntimeException("PublicationID not provided"));

            publication = publicationService.fetchOngoingPublicationByPublicId(publicationId)
                    .orElseThrow(() -> new RuntimeException("No ongoing assignment found by this ID: " + publicationId));

            participantData = fetchParticipantDataFromSession();

            // If there is no data in session, then we need to register new participant
            if (participantData == null) {
                VerticalLayout registrationBlock = createParticipantRegForm();
                mainContent.add(registrationBlock);
                mainContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, registrationBlock);
                return;
            }

            // If task was not started yet, task description should be shown
            if (participantData.getStartedAt() == null) {
                VerticalLayout taskDescription = createTaskDescription();
                mainContent.add(taskDescription);
                mainContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, taskDescription);
                return;
            }

            initNavBar();

            AssignmentExplorer assignmentExplorer = createAssignmentExplorer();
            mainContent.add(assignmentExplorer);
        } catch (Exception ex) {
            logger.error("Got an error during initialization assignment view", ex);
            VerticalLayout errorLayout = createNotFoundMessage();
            mainContent.add(errorLayout);
            mainContent.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, errorLayout);
        }
    }

    private AssignmentExplorer createAssignmentExplorer() {
        AssignmentExplorer assignmentExplorer = new AssignmentExplorer(participantData);
        assignmentExplorer.setSizeFull();
        assignmentExplorer.registerCallback(participant -> {
            try {
                participantData = participantService.saveParticipantData(participant);
                storeParticipantDataInSession(participantData);
            } catch (Exception ex) {
                logger.error("Cannot save participant data");
                NotificationHelper.displayError(getTranslation("participant_reg_form_error_msg"));
            }
        });
        return assignmentExplorer;
    }

    private void initNavBar() {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDateTime startedAt = participantData.getStartedAt();
        long secondsPassed = Duration.between(startedAt, currentDate).toSeconds();
        long timeLimit = participantData.getPublication().getTimeLimit() * 60L;

        BigDecimal timerTime = BigDecimal.valueOf(timeLimit);
        SimpleTimer timer = new SimpleTimer(timerTime.subtract(BigDecimal.valueOf(secondsPassed)));
        timer.getStyle().set("font-weight", "bold");
        timer.getStyle().set("font-size", "large");
        timer.setHours(true);
        timer.setMinutes(true);
        timer.setFractions(false);

        timer.start();
        timer.addTimerEndEvent(event -> NotificationHelper.displaySuccess("Timer ended!"));

        Button submitBtn = new Button(getTranslation("assignment_view_btn_submit"), VaadinIcon.PAPERPLANE.create());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitBtn.addClickListener(event -> {});

        H1 appTitle = new H1(getTranslation("app_title"));
        appTitle.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
                appTitle,
                timer,
                submitBtn
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(appTitle);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private VerticalLayout createNotFoundMessage() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames("bordered");
        layout.setWidth(800, Unit.PIXELS);

        Label errorMsg = new Label(getTranslation("assignment_view_msg_not_found"));
        errorMsg.setWidthFull();
        errorMsg.getStyle().set("text-align", "center");
        errorMsg.getStyle().set("color", "red");

        layout.add(errorMsg);

        return layout;
    }

    private VerticalLayout createTaskDescription() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames("bordered");
        layout.setWidth(1000, Unit.PIXELS);

        Task taskData = publication.getTask();

        H3 title = new H3(getTranslation("assignment_view_task_details_title", taskData.getTitle()));
        Label numOfProblemsMsg = new Label(
                getTranslation("assignment_view_task_details_number_of_problems", taskData.getProblems().size()));

        Label timeLimitMsg = new Label(
                getTranslation("assignment_view_task_details_time_limit", publication.getTimeLimit()));

        H4 descriptionTitle = new H4(getTranslation("assignment_view_task_details_description"));
        descriptionTitle.getStyle().set("margin-bottom", "0");

        Button startBtn = new Button(getTranslation("assignment_view_task_details_btn_start"), VaadinIcon.PLAY.create());
        startBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        startBtn.addClickListener(event -> {
            try {
                participantData.setStartedAt(LocalDateTime.now());
                participantData = participantService.saveParticipantData(participantData);
                UI.getCurrent().getPage().reload();
            } catch (Exception ex) {
                logger.error("Error during registration", ex);
                NotificationHelper.displayError(getTranslation("participant_reg_form_error_msg"));
            }
        });

        layout.add(title, numOfProblemsMsg, timeLimitMsg, descriptionTitle,
                new Html(String.format("<div>%s</div>", taskData.getDescription())), startBtn);

        layout.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, startBtn);

        return layout;
    }

    private VerticalLayout createParticipantRegForm() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames("bordered");
        layout.setWidth(800, Unit.PIXELS);

        ParticipantRegistrationForm registrationForm = new ParticipantRegistrationForm(new Participant());
        registrationForm.setWidthFull();
        registrationForm.registerSaveCallback(participant -> {
            try {
                participantData = participantService.createNewParticipant(participant, publication);
                storeParticipantDataInSession(participantData);
                UI.getCurrent().getPage().reload();
            } catch (Exception ex) {
                logger.error("Error during registration", ex);
                NotificationHelper.displayError(getTranslation("participant_reg_form_error_msg"));
            }
        });

        layout.add(registrationForm);

        return layout;
    }

    private void storeParticipantDataInSession(@NotNull Participant participant) {
        UI.getCurrent().getSession().setAttribute(publication.getPublicId(), participant);
    }

    private Participant fetchParticipantDataFromSession() {
        Object sessionData = UI.getCurrent().getSession().getAttribute(publication.getPublicId());
        if (sessionData == null) {
            return null;
        }

        if (sessionData instanceof Participant) {
            return (Participant) sessionData;
        }

        UI.getCurrent().getSession().setAttribute(publication.getPublicId(), null);
        return null;
    }

    @Override
    public String getPageTitle() {
        return getTranslation("page_title_home");
    }
}

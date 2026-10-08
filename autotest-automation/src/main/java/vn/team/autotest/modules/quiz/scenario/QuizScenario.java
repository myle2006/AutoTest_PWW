package vn.team.autotest.modules.quiz.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.quiz.page.QuizPage;
public final class QuizScenario extends AbstractModuleScenario { protected String route(){return "/quiz";} protected PwwModulePage page(){return new QuizPage(driver(),config());} }

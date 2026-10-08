package vn.team.autotest.modules.vocabulary.page;
import org.openqa.selenium.WebDriver; import vn.team.autotest.automation.base.PwwModulePage; import vn.team.autotest.core.config.AppConfig;
public final class VocabularyPage extends PwwModulePage { public VocabularyPage(WebDriver d, AppConfig c){super(d,c,"/admin/vocabulary");} }

package vn.team.autotest.modules.users.page;
import org.openqa.selenium.WebDriver; import vn.team.autotest.automation.base.PwwModulePage; import vn.team.autotest.core.config.AppConfig;
public final class UsersPage extends PwwModulePage { public UsersPage(WebDriver d, AppConfig c){super(d,c,"/admin/users");} }

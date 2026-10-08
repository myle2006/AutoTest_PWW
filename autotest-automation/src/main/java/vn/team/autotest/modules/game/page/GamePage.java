package vn.team.autotest.modules.game.page;
import org.openqa.selenium.WebDriver; import vn.team.autotest.automation.base.PwwModulePage; import vn.team.autotest.core.config.AppConfig;
public final class GamePage extends PwwModulePage { public GamePage(WebDriver d, AppConfig c){super(d,c,"/games");} }

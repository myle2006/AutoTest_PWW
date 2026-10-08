package vn.team.autotest.automation.base;

import org.testng.annotations.Test;
import vn.team.autotest.core.base.BaseScenario;

public abstract class AbstractModuleScenario extends BaseScenario {
    protected abstract String route();
    protected abstract PwwModulePage page();

    @Test(description = "Mở trang và xác minh nội dung chính")
    public final void verifyModulePage() {
        open(route());
        PwwModulePage modulePage = page();
        modulePage.waitUntilReady();
        modulePage.verifyPrimaryContent();
    }
}

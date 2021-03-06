package examples.lf5.InitUsingDefaultConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.lf5.DefaultLF5Configurator;
import java.io.IOException;
public class InitUsingDefaultConfigurator {
    private static Logger logger =
            Logger.getLogger(InitUsingDefaultConfigurator.class);
    public static void main(String[] args) throws IOException {
        DefaultLF5Configurator.configure();
        NDC.push("#23856");
        for (int i = 0; i < 10; i++) {
            logger.debug("Hello, my name is Homer Simpson.");
            logger.info("Mmmmmm .... Chocolate.");
            logger.warn("Mmm...forbidden donut.");
        }
        NDC.pop();
        NDC.remove();
        NDC.push("Another NDC");
        logger.fatal("Hello, my name is Bart Simpson.");
        logger.error("Hi diddly ho good neighbour.");
        NDC.pop();
        NDC.remove();
        InitUsingDefaultConfigurator.foo();
        InnerInitUsingDefaultConfigurator.foo();
        logger.info("Exiting InitUsingDefaultConfigurator.");
    }
    public static void foo() {
        logger.debug("Entered foo in InitUsingDefaultConfigurator class");
        NDC.push("#123456");
        logger.debug("Hello, my name is Marge Simpson.");
        logger.info("D'oh!! A deer! A female deer.");
        NDC.pop();
        NDC.remove();
    }
    public static class InnerInitUsingDefaultConfigurator {
        static Logger logger =
                Logger.getLogger(InnerInitUsingDefaultConfigurator.class.getName());
        static void foo() throws IOException {
            DefaultLF5Configurator.configure();
            logger.info("Entered foo in InnerInitUsingDefaultConfigurator class.");
        }
    }
}

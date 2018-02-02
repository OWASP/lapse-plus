package lapsePlus;

/*
 * LapsePlugin.java, version 2.8, 2010
 */
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lapsePlus.views.LapseView;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class LapsePlugin extends /* AbstractUI */Plugin {
    // The shared instance.
    private static LapsePlugin plugin;
    
    // Resource bundle.
    private ResourceBundle resourceBundle;
    LapseView lapseView = null;
    
    /** Setting that determine the logic of the slicer */
    private static int maxCallDepth = 10;
    public static int maxCallers = 10;
    public static boolean FOLLOW_INTO_FUNCTIONS = true;
    public static boolean LIMIT_PROP_DEPTH = true;

    public LapsePlugin() {
        super();
        plugin = this;
        try {
            resourceBundle = ResourceBundle.getBundle("lapse.LapsePluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     */
    public static LapsePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = LapsePlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static void setMaxCallDepth(int theMaxCallDepth) {
        maxCallDepth = theMaxCallDepth;
    }

    public static int getMaxCallDepth() {
        return maxCallDepth;
    }

    public static void setMaxCallers(int theMaxCallers) {
        maxCallers = theMaxCallers;
    }

    public static int getMaxCallers() {
        return maxCallers;
    }

    public void setLapseView(LapseView view) {
        this.lapseView = view;
    }

    public LapseView getLapseView() {
        return lapseView;
    }

    /**
     * Performs the Platform.getDebugOption true check on the provided trace
     * 
     * @param trace
     *            constant, defined in the Trace class
     * @return true if -debug is on for this plugin
     */
    public static boolean isDebugging(final String trace) {
        return 
            getDefault().isDebugging()
            && "true".equalsIgnoreCase(Platform.getDebugOption(trace)); //$NON-NLS-1$
    }

    /**
     * Outputs a message or an Exception if the current plug-in is debugging.
     * 
     * @param message
     *            if not null, message will be sent to standard out
     * @param e
     *            if not null, e.printStackTrace() will be called.
     */
    public static void trace(String trace_mode, String message, Throwable e) {
        if (isDebugging(trace_mode)) {
            Assert.isNotNull(message);
            if (e == null) {
                logInfo(message);
            } else {
                logError(message, e);
            }
        }
    }

    /**
     * Logging APIs. 
     * */
    
    public static final String SOURCE_DEBUG     = "lapse/source-debug";
    public static final String SINK_DEBUG       = "lapse/sink-debug";
    public static final String PROVENANCE_DEBUG = "lapse/provenance-debug";
    public static final String AST_PARSING      = "lapse/ast-parsing";
    public static final String SEARCH           = "lapse/search";
    public static final String RECURSION        = "lapse/recursion";
    public static final String ALL_DEBUG        = "lapse/debug";
    
    public static void logInfo(String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
    }

    public static void logError(Throwable exception) {
        logError("Unexpected Exception", exception);
    }

    public static void logError(String message, Throwable exception) {
        log(IStatus.ERROR, IStatus.OK, message, exception);
    }

    public static void log(int severity, int code, String message, Throwable exception) {
        log(createStatus(severity, code, message, exception));
    }

    public static IStatus createStatus(int severity, int code, String message, Throwable exception) {
        return new Status(severity, LapsePlugin.getDefault().getBundle().getSymbolicName(), code,
            message, exception);
    }

    private static void log(IStatus status) {
        LapsePlugin.getDefault().getLog().log(status);
    }
}

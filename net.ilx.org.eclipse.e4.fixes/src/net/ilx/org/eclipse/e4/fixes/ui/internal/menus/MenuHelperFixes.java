package net.ilx.org.eclipse.e4.fixes.ui.internal.menus;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;

import base org.eclipse.ui.internal.menus.MenuHelper;

/**
 * @author ilonca
 *
 */
public team class MenuHelperFixes {

	public static void error(String msg, Throwable error) {
		WorkbenchPlugin.log(msg, error);
	}

	/**
	 * Fix for bug 403509: https://bugs.eclipse.org/bugs/show_bug.cgi?id=403509.
	 *
	 * @author ilonca
	 *
	 */
	public class VisibleWhenFix playedBy MenuHelper {

		MExpression getVisibleWhen(IConfigurationElement commandAddition) <- replace
				MExpression getVisibleWhen(IConfigurationElement commandAddition);

		String getCommandId(IConfigurationElement element) -> String getCommandId(IConfigurationElement element);

		static callin MExpression getVisibleWhen(final IConfigurationElement commandAddition) {
			try {
				IConfigurationElement[] visibleConfig = commandAddition.getChildren(IWorkbenchRegistryConstants.TAG_VISIBLE_WHEN);
				if (visibleConfig.length > 0 && visibleConfig.length < 2) {
					IConfigurationElement[] visibleChild = visibleConfig[0].getChildren();
					if (visibleChild.length == 0) {
						String checkEnabled = visibleConfig[0].getAttribute(IWorkbenchRegistryConstants.ATT_CHECK_ENABLED);
						if (Boolean.parseBoolean(checkEnabled)) {
							final String commandId = getCommandId(commandAddition);
							if (commandId == null) {
								return null;
							}

							Expression visWhen = new Expression() {
								@Override
								public EvaluationResult evaluate(IEvaluationContext context) {
									EvaluationResult result = null;
									try {
										EHandlerService service = (EHandlerService) context.getVariable(EHandlerService.class.getName());
										ICommandService commandService = (ICommandService) context.getVariable(ICommandService.class.getName());
										if (null != commandId) {
											Command c = commandService.getCommand(commandId);
											ParameterizedCommand generateCommand = ParameterizedCommand.generateCommand(c, Collections.EMPTY_MAP);
											result = EvaluationResult.valueOf(service.canExecute(generateCommand));
										}
									} catch (Throwable t) {
										// IllegalStateException("Failed to evaluate context for command with id: " + commandId, t);
										String msg = "Unable to evaluate getVisibleWhen expression.";
										if (null != commandAddition) {
											String name = commandAddition.getName();
											IContributor contributor = commandAddition.getContributor();
											String contributorName = "unknown";
											if (null != contributor) {
												contributorName = contributor.getName();
											}
											String value = commandAddition.getValue();
											msg = String.format("Failed to evaluate context for commandAddition: (name=%s, provide=%s, value=%s)",
													name, contributorName, value);
										}
										error(msg, t);

										result = EvaluationResult.FALSE;
									}
									return result;
								}
							};
							MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
							exp.setCoreExpressionId("programmatic.value"); //$NON-NLS-1$
							exp.setCoreExpression(visWhen);
							return exp;
						}
					} else if (visibleChild.length > 0) {
						Expression visWhen = ExpressionConverter.getDefault().perform(visibleChild[0]);
						MCoreExpression exp = UiFactoryImpl.eINSTANCE.createCoreExpression();
						exp.setCoreExpressionId("programmatic.value"); //$NON-NLS-1$
						exp.setCoreExpression(visWhen);
						return exp;
						// visWhenMap.put(configElement, visWhen);
					}
				}
			} catch (InvalidRegistryObjectException e) {
				// visWhenMap.put(configElement, null);
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// visWhenMap.put(configElement, null);
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

	}

}

package play.modules.resteasy.crud;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

import org.jboss.resteasy.links.ELProvider;

public class PlayELProvider implements ELProvider {
	public ELContext getContext(final ELContext ctx) {
		return new ELContext() {

			private PlayFunctionMapper functionMapper;

			@Override
			public ELResolver getELResolver() {
				return ctx.getELResolver();
			}

			@Override
			public FunctionMapper getFunctionMapper() {
				if (functionMapper == null)
					functionMapper = new PlayFunctionMapper(ctx
							.getFunctionMapper());
				return functionMapper;
			}

			@Override
			public VariableMapper getVariableMapper() {
				return ctx.getVariableMapper();
			}
		};
	}

}

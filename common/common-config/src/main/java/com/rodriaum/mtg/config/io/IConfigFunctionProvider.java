package com.rodriaum.mtg.config.io;

import java.util.List;
import com.rodriaum.mtg.config.ConfigFunction;
import com.rodriaum.mtg.interfaces.ILogger;
import com.rodriaum.mtg.interfaces.IMaterialReader;

public interface IConfigFunctionProvider
{
	public <T> ConfigFunction<T> getConfigFunction(String name, T holder, List<String> args, ILogger logger, IMaterialReader materialReader);
}

package com.rodriaum.mtg.customobject.bo2;

import com.rodriaum.mtg.customobject.CustomObject;
import com.rodriaum.mtg.customobject.CustomObjectLoader;
import com.rodriaum.mtg.customobject.config.io.FileSettingsReaderBO4;
import com.rodriaum.mtg.interfaces.ILogger;

import java.io.File;

public class BO2Loader implements CustomObjectLoader
{
	@Override
	public CustomObject loadFromFile(String objectName, File file, ILogger logger)
	{
		return new BO2(new FileSettingsReaderBO4(objectName, file, logger));
	}

	@Override
	public void onShutdown() { }
}

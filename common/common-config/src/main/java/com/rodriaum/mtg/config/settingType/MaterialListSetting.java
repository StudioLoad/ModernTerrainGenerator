package com.rodriaum.mtg.config.settingType;

import java.util.ArrayList;

import com.rodriaum.mtg.exceptions.InvalidConfigException;
import com.rodriaum.mtg.interfaces.IMaterialReader;
import com.rodriaum.mtg.util.helpers.StringHelper;
import com.rodriaum.mtg.util.materials.LocalMaterialData;

/**
 * Reads and writes a material. Materials are read using
 * {@link OTG#readMaterial(String)} and written using
 * {@link LocalMaterialData#toString()}.
 */
public class MaterialListSetting extends Setting<ArrayList<LocalMaterialData>>
{
	private final String[] defaultValue;
	private boolean processedMaterials;
	private ArrayList<LocalMaterialData> defaultMaterials;

	public MaterialListSetting(String name, String[] defaultValue)
	{
		super(name);
		this.defaultValue = defaultValue;
	}

	@Override
	public ArrayList<LocalMaterialData> getDefaultValue(IMaterialReader materialReader)
	{
		if(!this.processedMaterials)
		{
			this.processedMaterials = true;
			ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
			for(String defaultMaterial : this.defaultValue)
			{
				LocalMaterialData material = null;
				try {
					material = materialReader.readMaterial(defaultMaterial);
				} catch (InvalidConfigException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(material != null)
				{
					materials.add(material);
				}
			}
			this.defaultMaterials = materials;
		}
		return this.defaultMaterials;
	}

	@Override
	public ArrayList<LocalMaterialData> read(String string, IMaterialReader materialReader) throws InvalidConfigException
	{
		String[] materialNames = string.split(",(?![^\\(\\[]*[\\]\\)])"); // Splits on any comma not inside brackets
		ArrayList<LocalMaterialData> materials = new ArrayList<LocalMaterialData>();
		for(String materialName : materialNames)
		{
			LocalMaterialData material = materialReader.readMaterial(materialName.trim());
			materials.add(material);
		}
		return materials;
	}
	
	@Override
	public String write(ArrayList<LocalMaterialData> value)
	{
		return StringHelper.join(value, ", ");
	}
}

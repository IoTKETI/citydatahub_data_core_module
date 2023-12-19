package kr.re.keti.sc.datamanager.common.datamapperhandler;

import java.beans.PropertyEditorSupport;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionServerType;

/**
 * RestController Enum Parsing Handler (ProvisionServerType)
 */
public class ProvisionServerTypePropertyEditor extends PropertyEditorSupport {
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		ProvisionServerType provisionServerType = ProvisionServerType.parseType(text);
		super.setValue(provisionServerType);
	}
}

package kr.re.keti.sc.datamanager.common.datamapperhandler;

import java.beans.PropertyEditorSupport;

import kr.re.keti.sc.datamanager.common.code.DataManagerCode.ProvisionProtocol;

/**
 * RestController Enum Parsing Handler (ProvisionProtocol)
 */
public class ProvisionProtocolTypePropertyEditor extends PropertyEditorSupport {
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		ProvisionProtocol provisionProtocol = ProvisionProtocol.parseType(text);
		super.setValue(provisionProtocol);
	}
}

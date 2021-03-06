package one.atticus.core.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class Deal extends AbstractJson {
    @XmlElement(name = "deal_id")
    public Integer dealId;

    @XmlElement(name = "account_id")
    public Integer accountId;

    @XmlElement(name = "contract_id")
    public Integer contractId;

    @XmlElement
    public String title;

    @XmlElement
    public List<DealDialog> dialog;

    @XmlElement
    public List<Party> parties;

    @XmlElement
    public Long created;

    @XmlElement
    public Long updated;
}

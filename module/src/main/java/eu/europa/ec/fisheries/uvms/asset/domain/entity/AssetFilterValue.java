package eu.europa.ec.fisheries.uvms.asset.domain.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import static eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetFilterValue.ASSETFILTER_VALUE_FIND_ALL;
import static eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetFilterValue.ASSETFILTER_VALUE_GETBYID;
import static eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetFilterValue.ASSETFILTER_VALUE_CLEAR;
import static eu.europa.ec.fisheries.uvms.asset.domain.entity.AssetFilterValue.ASSETFILTER_RETRIEVE_VALUES_FOR_QUERY;

@Entity
@Table(name = "assetfiltervalue") // get index?

		@NamedQuery(name=ASSETFILTER_VALUE_FIND_ALL, query="SELECT a FROM AssetFilterValue a")
		@NamedQuery(name=ASSETFILTER_VALUE_GETBYID, query="SELECT a FROM AssetFilterValue a where a.id = :id")
		@NamedQuery(name=ASSETFILTER_VALUE_CLEAR, query="DELETE  FROM AssetFilterValue a where a.assetFilterQuery = :assetFilterQuery")
		@NamedQuery(name=ASSETFILTER_RETRIEVE_VALUES_FOR_QUERY, query="SELECT a FROM AssetFilterValue a where a.assetFilterQuery = :assetFilterQuery")

public class AssetFilterValue implements Serializable{

	public static final String ASSETFILTER_VALUE_FIND_ALL ="assetfiltervalue.findAll";
	public static final String ASSETFILTER_VALUE_GETBYID = "assetfiltervalue.getbyid";
	public static final String ASSETFILTER_VALUE_CLEAR = "assetfiltervalue.clear";
	public static final String ASSETFILTER_RETRIEVE_VALUES_FOR_QUERY = "assetfiltervalue.retrievevaluesforquery";

	private static final long serialVersionUID = 8898101336225121988L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;

    @Column(name = "valuenumber")
    private Double valueNumber;
    
    @Size(max = 255)
    @Column(name = "valuestring")
    private String valueString;
    
    @Size(max = 255)
    @Column(name = "operator")
    private String operator;
    
    @JsonbTransient
    @ManyToOne
    @JoinColumn(name = "assetfilterquery", foreignKey = @ForeignKey(name = "assetfiltervalue_assetfilterquery_fk"))
    private AssetFilterQuery assetFilterQuery;

    public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
	
	public Double getValueNumber() {
		return valueNumber;
	}

	public void setValueNumber(Double valueNumber) {
		this.valueNumber = valueNumber;
	}

	public String getValueString() {
		return valueString;
	}

	public void setValueString(String valueString) {
		this.valueString = valueString;
	}

	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}

	public AssetFilterQuery getAssetFilterQuery() {
		return assetFilterQuery;
	}

	public void setAssetFilterQuery(AssetFilterQuery assetFilterQuery) {
		this.assetFilterQuery = assetFilterQuery;
	}

}


package io.renren.entity;

/**
 * 列的属性
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月20日 上午12:01:45
 */
public class ColumnEntity {
	//列名
    private String columnName;
    //列名类型
    private String dataType;
    //列名备注
    private String comments;
    
    //属性名称(第一个字母大写)，如：user_name => UserName
    private String attrName;
    //属性名称(第一个字母小写)，如：user_name => userName
    private String attrname;
    //属性类型
    private String attrType;
    //auto_increment
    private String extra;
    
    private String characterMaximumLength;    //字符类型长度
    private String numericPrecision;          //数字类型长度
    private String numericScale;              //数字类型精度
    private String columnComment;              
    private String columnKey;              
    
	public ColumnEntity() {
		super();
	}
	
	public ColumnEntity(String columnName, String dataType, String extra, String characterMaximumLength,
			String numericPrecision, String numericScale, String columnComment, String columnKey) {
		super();
		this.columnName = columnName;
		this.dataType = dataType;
		this.extra = extra;
		this.characterMaximumLength = characterMaximumLength;
		this.numericPrecision = numericPrecision;
		this.numericScale = numericScale;
		this.columnComment = columnComment;
		this.columnKey = columnKey;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getAttrname() {
		return attrname;
	}
	public void setAttrname(String attrname) {
		this.attrname = attrname;
	}
	public String getAttrName() {
		return attrName;
	}
	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}
	public String getAttrType() {
		return attrType;
	}
	public void setAttrType(String attrType) {
		this.attrType = attrType;
	}
	public String getExtra() {
		return extra;
	}
	public void setExtra(String extra) {
		this.extra = extra;
	}
	public String getCharacterMaximumLength() {
		return characterMaximumLength;
	}
	public void setCharacterMaximumLength(String characterMaximumLength) {
		this.characterMaximumLength = characterMaximumLength;
	}
	public String getNumericPrecision() {
		return numericPrecision;
	}
	public void setNumericPrecision(String numericPrecision) {
		this.numericPrecision = numericPrecision;
	}
	public String getNumericScale() {
		return numericScale;
	}
	public void setNumericScale(String numericScale) {
		this.numericScale = numericScale;
	}

	public String getColumnComment() {
		return columnComment;
	}

	public void setColumnComment(String columnComment) {
		this.columnComment = columnComment;
	}

	public String getColumnKey() {
		return columnKey;
	}

	public void setColumnKey(String columnKey) {
		this.columnKey = columnKey;
	}
	
	
}

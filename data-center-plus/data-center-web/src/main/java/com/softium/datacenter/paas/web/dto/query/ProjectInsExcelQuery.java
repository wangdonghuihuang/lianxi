package com.softium.datacenter.paas.web.dto.query;


/**
 * @description:
 * @author: york
 * @create: 2020-08-03 18:24
 **/
public class ProjectInsExcelQuery extends ExcelModelQuery {
    public static final String PROJECTID = "projectId";
    public static final String BUSINESSTYPE = "businessType";
    //public static final String PROJECTINSTITUTIONID = "projectInstitutionId";
    public static final String PROJECTINSTITUTIONCODE = "projectInstitutionCode";
    public static final String PROJECTINSTITUTIONNAME = "projectInstitutionName";
    public String projectId(){
        return (String) get(PROJECTID);
    }
    public String businessType(){return (String) get(BUSINESSTYPE);}
    //public String projectInstitutionId(){return (String) get(PROJECTINSTITUTIONID);}
    public String projectInstitutionCode(){return (String) get(PROJECTINSTITUTIONCODE);}
    public String projectInstitutionName(){return (String) get(PROJECTINSTITUTIONNAME);}
}

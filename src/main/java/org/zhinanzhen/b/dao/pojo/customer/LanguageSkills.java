package org.zhinanzhen.b.dao.pojo.customer;

import lombok.Data;

import java.util.List;

@Data
public class LanguageSkills {

    private int isProficiencyEnglish;

    private List<EnglishSkills> englishSkillsList;

    private int isFirstLanguage;

    private List<AllLanguages> allLanguagesList;

}


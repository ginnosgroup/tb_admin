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

@Data
 class EnglishSkills {

    private String type;

    private String testDate;

    private String location;

    private String referenceNumber;

    private String listeningScore;

    private String readingScore;

    private String writingScore;

    private String speakingScore;

    private String overallScoreOrGrade;
}

@Data
 class AllLanguages {

    private String Language;

    private String LevelOfProficiency;

    private String isMainLanguages;
}
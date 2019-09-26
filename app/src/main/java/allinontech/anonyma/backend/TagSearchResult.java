package allinontech.anonyma.backend;

public class TagSearchResult {
    String tagName;
    String results;

    public TagSearchResult( String tagName, String results){
        this.tagName = tagName;
        this.results = results;
    }

    public String getResults() {
        return results;
    }

    public String getTagName() {

        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setResults(String results) {
        this.results = results;
    }
}

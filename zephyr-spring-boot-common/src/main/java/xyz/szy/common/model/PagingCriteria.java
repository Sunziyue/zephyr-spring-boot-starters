package xyz.szy.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class PagingCriteria extends Criteria implements Serializable {
    private static final long serialVersionUID = 2598875146576926658L;
    public static final int SORT_TYPE_ASC = 1;
    public static final int SORT_TYPE_DESC = 2;
    @JsonIgnore
    protected Integer pageNo = 1;
    @JsonIgnore
    protected Integer pageSize = 20;
    @JsonIgnore
    protected Boolean hasNext = true;
    protected Boolean skipCount;
    protected String sortBy;
    protected Integer sortType;

    public PagingCriteria() {
    }

    @JsonIgnore
    public Boolean hasNext() {
        return this.hasNext;
    }

    public void nextPage() {
        if (this.pageNo == null) {
            this.pageNo = 1;
        }

        this.pageNo = this.pageNo + 1;
    }

    public Integer getLimit() {
        PageInfo pageInfo = new PageInfo(this.pageNo, this.pageSize);
        return pageInfo.getLimit();
    }

    public Integer getOffset() {
        PageInfo pageInfo = new PageInfo(this.pageNo, this.pageSize);
        return pageInfo.getOffset();
    }

    public Map<String, Object> toMap() {
        return super.toMap();
    }
}

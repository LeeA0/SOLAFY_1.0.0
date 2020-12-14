package com.solafy.mapper;
/**
 * ProblemMapper
 * @author Lee AYoung
 * @since 2020-12-13
 */
import java.util.List;

import com.solafy.model.HashTagDto;
import com.solafy.model.ProblemDto;

public interface ProblemMapper {
	// 문제집에 포함되는 문제리스트 반환
	List<ProblemDto> selectProblemList(int problemSetNo);
	// 문제하나 정보 반환(상세보기 용)
	ProblemDto selectProblem(int problemNo);
	// 문제의 카테고리 반환 
	
	// 문제의 해시태그 반환 : hashtagmapping테이블에서 문제 번호를 받아 반환
	List<HashTagDto> selectHashtagList(int problemNo);
	// 해시태그로 문제 검색
	List<ProblemDto> selectProblemByHashTag(String hashTag);
	// 문제이름(일부)으로 검색
	List<ProblemDto> selectProblemByName(String title);
	// 카테고리를 이용한 문제 검색
	
	// 문제 평가하기(서비스단에서 계산해서 넣어줘야할 거 같은데 평균을 어찌줄까?)
	int updateStarScore(double starScore);
	// 문제 등록
	int insertProblem(ProblemDto problemDto);
	// 문제 수정
	int updateProblem(ProblemDto problemDto);
	// 문제 삭제
	int deleteProblem(int problemNo);
}

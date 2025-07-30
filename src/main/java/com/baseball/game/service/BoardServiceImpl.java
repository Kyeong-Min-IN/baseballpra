package com.baseball.game.service;
import com.baseball.game.dto.BoardDto;
import com.baseball.game.dto.BoardRequestDto;
import com.baseball.game.dto.BoardPageResponse;
import com.baseball.game.mapper.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
@Service
public class BoardServiceImpl implements BoardService{
	
	private final BoardMapper mapper;
	@Autowired
	public BoardServiceImpl(BoardMapper mapper) {
		this.mapper=mapper;
	}
	@Override
	public ArrayList<BoardDto> getListCategory(String category){
		ArrayList<BoardDto> m=mapper.getListCategory(category);
		return m;
	}
	@Override
	public ArrayList<BoardDto> getList(){
		ArrayList<BoardDto> m=mapper.getList();
		return m;
	}
	@Override
	public void write(BoardRequestDto d) {
		mapper.write(d);
	}
	@Override
	public void modify(int no,String text) {
		mapper.modify(no,text);
	}
	
	@Override
	public void delete(int no) {
		mapper.delete(no);
	}
	@Override
	public BoardDto getBoard(int no) {
		return mapper.getBoard(no);
	}

	@Override
	public BoardPageResponse getPagedList(int page, int size) {
		int offset = (page - 1) * size;
		ArrayList<BoardDto> list = mapper.getPagedList(offset, size);
		int totalCount = mapper.getTotalCount();
		return new BoardPageResponse(totalCount, list);
	}
	@Override
	public BoardPageResponse getPagedListCategory(int page, int size, String category) {
		int offset = (page - 1) * size;
		ArrayList<BoardDto> list = mapper.getPagedListCate(offset, size, category);
		int totalCount = mapper.getTotalCount();
		return new BoardPageResponse(totalCount, list);
	}
}

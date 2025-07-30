package com.baseball.game.mapper;
import com.baseball.game.dto.MemberDto;
public interface MemberMapper {
	public boolean login(String Id,String Pw);
	public boolean checkId(String Id);
	public void register(String Id,String Pw,String Team);
	public MemberDto member(String Id);
}

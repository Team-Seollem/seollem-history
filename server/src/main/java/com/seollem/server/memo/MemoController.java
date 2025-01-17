package com.seollem.server.memo;

import com.seollem.server.book.entity.Book;
import com.seollem.server.book.service.BookService;
import com.seollem.server.file.FileUploadService;
import com.seollem.server.member.entity.Member;
import com.seollem.server.member.service.MemberService;
import com.seollem.server.util.GetEmailFromHeaderTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/memos")
@Slf4j
@Validated
@RequiredArgsConstructor
public class MemoController {

    private final MemoService memoService;
    private final MemoMapper mapper;
    private final MemberService memberService;
    private final GetEmailFromHeaderTokenUtil getEmailFromHeaderTokenUtil;
    private final BookService bookService;
    private final FileUploadService fileUploadService;

    @PostMapping("/{book-id}")
    public ResponseEntity postMemo(@RequestHeader Map<String, Object> requestHeader,
                                   @Valid @RequestBody MemoDto.Post post,
                                   @Positive @PathVariable("book-id") long bookId) {
        String email = getEmailFromHeaderTokenUtil.getEmailFromHeaderToken(requestHeader);
        Member member = memberService.findVerifiedMemberByEmail(email);

        bookService.verifyMemberHasBook(bookId, member.getMemberId());

        Memo memoOfBook = mapper.memoPostToMemo(post);
        Book book = bookService.findVerifiedBookById(bookId);
        memoOfBook.setMember(member);
        memoOfBook.setBook(book);

        Memo memo = memoService.createMemo(memoOfBook);
        MemoDto.Response response = mapper.memoToMemoResponse(memo);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 이미지 등록
    @PostMapping("/image-memo")
    public ResponseEntity postImageMemo(@RequestHeader Map<String, Object> requestHeader,
                                        @RequestPart MultipartFile file){
        String email = getEmailFromHeaderTokenUtil.getEmailFromHeaderToken(requestHeader);
        Member member = memberService.findVerifiedMemberByEmail(email);

        String url = fileUploadService.createImageMemo(file);

        return new ResponseEntity<>(url,HttpStatus.CREATED);
    }

    @PatchMapping("/{memo-id}")
    public ResponseEntity patchMemo(@RequestHeader Map<String, Object> requestHeader,
                                    @PathVariable("memo-id") @Positive long memoId,
                                    @Valid @RequestBody MemoDto.Patch patch) {
        String email = getEmailFromHeaderTokenUtil.getEmailFromHeaderToken(requestHeader);
        Member member = memberService.findVerifiedMemberByEmail(email);

        memoService.verifyMemberHasMemo(memoId, member.getMemberId());

        patch.setMemoId(memoId);
        Memo PatchMemo = mapper.memoPatchToMemo(patch);

        Memo memo = memoService.updateMemo(PatchMemo);
        MemoDto.Response response = mapper.memoToMemoResponse(memo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/random")
    public ResponseEntity randomMemo(@RequestHeader Map<String, Object> requestHeader) {
        String email = getEmailFromHeaderTokenUtil.getEmailFromHeaderToken(requestHeader);
        Member member = memberService.findVerifiedMemberByEmail(email);

        List<Memo> random = memoService.randomMemo(member);
        List<MemoDto.RandomResponse> responses =
                random.stream()
                        .map(memo -> mapper.momoToMemoRandom(memo))
                        .collect(Collectors.toList());

        return new ResponseEntity<>(responses, HttpStatus.OK);

    }

    @DeleteMapping("/{memo-id}")
    public ResponseEntity deleteMemo(@RequestHeader Map<String, Object> requestHeader,
                                     @PathVariable("memo-id") @Positive long memoId) {
        String email = getEmailFromHeaderTokenUtil.getEmailFromHeaderToken(requestHeader);
        Member member = memberService.findVerifiedMemberByEmail(email);

        memoService.verifyMemberHasMemo(memoId, member.getMemberId());

        memoService.deleteMemo(memoId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

}

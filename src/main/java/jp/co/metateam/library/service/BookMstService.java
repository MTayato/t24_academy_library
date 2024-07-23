package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import io.micrometer.common.util.StringUtils;
import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.model.Review;
import jp.co.metateam.library.model.ReviewDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.ReviewRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public BookMstService(BookMstRepository bookMstRepository, StockRepository stockRepository, ReviewRepository reviewRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<BookMst> findAll() {
        return this.bookMstRepository.findAll();
    }

    public Optional<BookMst> findById(Long id) {
        return this.bookMstRepository.findById(id);
    }

    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findAll();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            List<Stock> stockCount = this.stockRepository.findByBookMstIdAndStatus(book.getId(),
                    Constants.STOCK_AVAILABLE);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDto.setStockCount(stockCount.size());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        try {
            BookMst book = new BookMst();

            book.setTitle(bookMstDto.getTitle());
            book.setIsbn(bookMstDto.getIsbn());

            // データベースへの保存
            this.bookMstRepository.save(book);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(Long id, BookMstDto bookMstDto) throws Exception {
        try {
            // 既存レコード取得
            BookMst updateTargetBook = this.bookMstRepository.findById(id).orElse(null);
            if (updateTargetBook == null) {
                throw new Exception("BookMst record not found.");
            }

            updateTargetBook.setTitle(bookMstDto.getTitle());
            updateTargetBook.setIsbn(bookMstDto.getIsbn());

            // データベースへの保存
            this.bookMstRepository.save(updateTargetBook);
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean isValidTitle(String title, Model model) {
        if (StringUtils.isEmpty(title)) {
            model.addAttribute("errTitle", "書籍タイトルは必須");
            return true;
        }
        return false;
    }

    public boolean isValidIsbn(String isbn, Model model) {
        if (StringUtils.isEmpty(isbn) || isbn.length() != 13) {
            model.addAttribute("errISBN", "ISBNは13文字で入力してください");
            return true;
        }
        return false;
    }

    @Transactional
    public List<Review> reviewIndex(Long id){
        List<Review> reviewList = this.reviewRepository.reviewCheck(id);

        return reviewList;
    }

    // レビューバリデーション
    public boolean isValidBody(String body, Model model) {
        if (StringUtils.isEmpty(body) || body.length() != 140) {
            model.addAttribute("errBody", "レビュー本文は140字以内で記述してください");
            return true;
        }
        return false;
    }
    public boolean isValidScore(Integer score, Model model) {
        if (score == null || score < 0 || score > 9) {
            model.addAttribute("errScore", "評価は0から9までの整数で入力してください");
            return false; // 評価が無効な場合は false を返す
        }
        return true; // 評価が有効な場合は true を返す
    }
    @Transactional
    public void reviewSave(ReviewDto reviewDto) {
        try {
            Review review = new Review();

            review.setScore(reviewDto.getScore());
            review.setBody(reviewDto.getBody());

            // データベースへの保存
            this.reviewRepository.save(review);
        } catch (Exception e) {
            throw e;
        }
    }
}
    
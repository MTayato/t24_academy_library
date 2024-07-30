package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.RentableNumDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.repository.RentalManageRepository;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository, RentalManageRepository rentalManageRepository){
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }
    
    @Transactional
    public List <Stock> findStockAvailableAll() {
        List <Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional 
    public void save(StockDto stockDto) throws Exception {
        try {
        Stock stock = new Stock();
        BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
        if (bookMst == null) {
            throw new Exception("BookMst record not found.");
        }

        stock.setBookMst(bookMst);
        stock.setId(stockDto.getId());
        stock.setStatus(stockDto.getStatus());
        stock.setPrice(stockDto.getPrice());

            // データベースへの保存
        this.stockRepository.save(stock);
        } catch (Exception e) {
          throw e;
        }
    }

    @Transactional 
    public void update(String id, StockDto stockDto) throws Exception {
        try {
        Stock stock = findById(id);
        if (stock == null) {
            throw new Exception("Stock record not found.");
        }

        BookMst bookMst = stock.getBookMst();
        if (bookMst == null) {
            throw new Exception("BookMst record not found.");
        }

        stock.setId(stockDto.getId());
        stock.setBookMst(bookMst);
        stock.setStatus(stockDto.getStatus());
        stock.setPrice(stockDto.getPrice());

            // データベースへの保存
        this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public int rentalDateCount(String stockid, Date expectedDate) {
        return this.rentalManageRepository.rentalDateCount(stockid, expectedDate);
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<CalendarDto> generateStocksForCalendar(Integer year, Integer month, Integer daysInMonth) {
        List<CalendarDto> calendarDtoList = new ArrayList<>();
        
        List<BookMst> bookList = this.bookMstRepository.findAll(); //書籍全件取得
        LocalDate today = LocalDate.now();

        for(int bookOfOne = 0; bookOfOne < bookList.size(); bookOfOne++) {
            BookMst book = bookList.get(bookOfOne); //書籍リストのうち一つを取得
            List<Stock> stockList = this.stockRepository.findByBookMstIdAndStatus(book.getId(),Constants.STOCK_AVAILABLE); //利用可能かつ指定したIDに一致するものを全件取得しリスト化
            
            if(stockList.size() > 0){ //利用可能在庫数が0の時表示させない
                CalendarDto calendarDto = new CalendarDto();
                calendarDto.setTitle(book.getTitle());
                calendarDto.setStockCount(stockList.size());

                for(int oneday = 1; oneday <= daysInMonth; oneday++){ //日付ごとのループ
                    LocalDate  localExpectedDate = LocalDate.of(year,month,oneday);
                    Date expectedDate = Date.from( localExpectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    calendarDto.rentableNum.add(rentalCount(stockList, expectedDate, today,  localExpectedDate));
                }
                calendarDtoList.add(calendarDto);
            }
        }
        return calendarDtoList;
    }

    public RentableNumDto rentalCount(List<Stock> stockList, Date expectedDate, LocalDate today, LocalDate  localExpectedDate){
        RentableNumDto rentableNumDto = new RentableNumDto(); 
        int forDayOfExpected = 0;
        
        if (!( localExpectedDate.isBefore(today))){
            
            for(int idOfOne = 0; idOfOne < stockList.size(); idOfOne++){
                Stock stock = stockList.get(idOfOne); 
                if(rentalDateCount(stock.getId(), expectedDate) == 0){
                    forDayOfExpected++;
                    rentableNumDto.stockIdList.add(stock.getId());
                }
            }
        }
        if(forDayOfExpected == 0){
            rentableNumDto.setDayOfExpected("✕");
        }else{
            rentableNumDto.setDayOfExpected(forDayOfExpected);
            rentableNumDto.setLinkDate(expectedDate);
        }
        return rentableNumDto;
    }
    @Transactional 
    public List<Stock> getList(List<String> stockIdList){
        List <Stock> stockList = new ArrayList<>();

        for(int i = 0; i < stockIdList.size(); i++){
            String stockId = stockIdList.get(i);
            Optional<Stock> optionalStock = stockRepository.findById(stockId);
            Stock stock = optionalStock.orElse(new Stock());
            stockList.add(stock);  
        }
        return stockList;
    }
}
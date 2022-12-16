package com.sohwakmo.cucumbermarket.service;

import com.sohwakmo.cucumbermarket.domain.Member;
import com.sohwakmo.cucumbermarket.domain.Product;
import com.sohwakmo.cucumbermarket.domain.ProductOfInterested;
import com.sohwakmo.cucumbermarket.dto.ProductCreateDto;
import com.sohwakmo.cucumbermarket.dto.ProductOfInterestedRegisterOrDeleteOrCheckDto;
import com.sohwakmo.cucumbermarket.dto.ProductUpdateDto;
import com.sohwakmo.cucumbermarket.repository.MemberRepository;
import com.sohwakmo.cucumbermarket.repository.ProductOfInterestedRepository;
import com.sohwakmo.cucumbermarket.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final ProductOfInterestedRepository productOfInterestedRepository;

    public Page<Product> read(Pageable pageable) { // 전체 상품 목록
        log.info("read()");

        return productRepository.findByStatusOrderByProductNoDesc(false, pageable);
    }

    public List<Product> readByLikeCountDesc(){
        log.info("readByLikeCountDesc()");
        return productRepository.findByOrderByLikeCountDescProductNoDesc();
    }

    public Product read(Integer productNo) { // 상품 조회
        log.info("read(productNo = {})", productNo);

        return productRepository.findById(productNo).get();
    }

    @Transactional
    public Product detail(Integer productNo) {
        log.info("detail(productNo = {})", productNo);

        Product product = productRepository.findById(productNo).get();
        log.info("product = {}", product);
        product.updateClickCount(product.getClickCount()+1);
        log.info("product = {}", product);

        return product;
    }

    public Page<Product> search(String type, String keyword, Pageable pageable) {
        log.info("search(type = {}, keyword = {})", type, keyword);

//        List<Product> list = new ArrayList<>();
        Page<Product> list;

        switch(type) {
        case "all": // 전체 검색이라면 
            log.info("type = {}", type);

            if( keyword.equals("")) { // 검색 내용이 없으면 전부 검색
                log.info("keyword = null");
                list = productRepository.findByStatusOrderByProductNoDesc(false, pageable);
            } else { // 검색 내용이 있으면 내용 검색
                log.info("keyword = notNull");
                list = productRepository.searchByKeyword(false, keyword, keyword, keyword, pageable);
            }

            break;
        default: // 전체 검색이 아니라면
            log.info("type = {}", type);

            if( keyword.equals("")) { // 검색 내용이 없으면
                log.info("keyword = null");
                list = productRepository.findByStatusAndDealAddressIgnoreCaseContainingOrderByProductNoDesc(false, type, pageable);
            } else { // 검색 내용이 있으면 내용 검색
                log.info("keyword = notNull");
                list = productRepository.searchByTypeAndKeyword(false, type, keyword, keyword, keyword, pageable);
            }

        }

        log.info("list = {}", list);

        return list;
    }

    @Transactional
    public void addInterested(ProductOfInterestedRegisterOrDeleteOrCheckDto dto) {
        log.info("addInterested(dto = {}", dto);

        Product product = productRepository.findById(dto.getProductNo()).get();
        log.info("product = {}", product);

        ProductOfInterested entity = ProductOfInterested.builder()
                .member(dto.getMemberNo()).product(product)
                .build();
        log.info("entity = {}", entity);

        productOfInterestedRepository.save(entity); // DB에 insert

        product.updateLikeCount(product.getLikeCount()+1); // 상품의 관심목록 1증가
        log.info("product = {}", product);
    }

    @Transactional
    public void deleteInterested(ProductOfInterestedRegisterOrDeleteOrCheckDto dto) {
        log.info("deleteInterested(dto = {})", dto);

        Product product = productRepository.findById(dto.getProductNo()).get();
        log.info("product = {}", product);

        product.updateLikeCount(product.getLikeCount()-1);

        productOfInterestedRepository.deleteByMemberAndProduct(dto.getMemberNo(), product);
    }

    public String checkInterestedProduct(ProductOfInterestedRegisterOrDeleteOrCheckDto dto) {
        log.info("checkInterestedProduct(dto = {})", dto);

        Product product = productRepository.findById(dto.getProductNo()).get();
        log.info("product = {}", product);

        Optional<ProductOfInterested> result = productOfInterestedRepository.findByMemberAndProduct(dto.getMemberNo(), product);
        if (result.isPresent()) {
            log.info("result = {}", result);
            return "ok";
        } else {
            log.info("없음");
            return "nok";
        }

    }

    @Transactional
    public List<Product> interestedRead(Integer memberNo) {
        log.info("interested(memberNo = {})", memberNo);

        List<ProductOfInterested> list = productOfInterestedRepository.findByMember(memberNo);
        log.info("list = {}", list);

        List<Product> productsList = new ArrayList<>();
        for(ProductOfInterested s : list) {
            productsList.add(s.getProduct());
        }
        log.info("productsList = {}", productsList);

        return productsList;
    }

    //마이페이지 판매내역-진행중 검색
    public List<Product> proceedListRead(Integer memberNo) {
        log.info("proceedListRead(memberNo={})", memberNo);

        Member member = memberRepository.findById(memberNo).get();
        log.info("member={}", member);

        List<Product> list = productRepository.findByMemberAndStatus(member,false);
        log.info("proceeding list = {}", list);

        return list;

    }

    //마이페이지 판매내역-거래완료 검색
    public List<Product> completedListRead(Integer memberNo) {
        log.info("completedListRead(memberNo={})",memberNo);

        Member member = memberRepository.findById(memberNo).get();
        log.info("member={}", member);

        List<Product> list = productRepository.findByMemberAndStatus(member,true);
        log.info("completed list = {}", list);

        return list;
    }
    
    //마이페이지 구매목록
    public List<Product> buyMyListRead(Integer memberNo) {
        log.info("buyMyListRead(memberNo={})", memberNo);

        List<Product> list = productRepository.findByBoughtMemberNo(memberNo);
        log.info("list = {}", list);

        return list;
    }

    @Transactional
    public List<Product> myProductListRead(Integer memberNo) {
        log.info("myProductListRead(memberNo = {})", memberNo);

        Member member = memberRepository.findById(memberNo).get();
        log.info("member = {}", member);

        List<Product> list = productRepository.findByMember(member);
        log.info("list = {}", list);

        return list;
    }

    @Transactional
    public void dealStatusIng(Integer productNo) {
        log.info("dealStatusIng(productNo = {})", productNo);

        Product product = productRepository.findById(productNo).get();
        log.info("product = {}", product);


        product.updateStatusAndBoughtMemberNo(false, null);
    }

    @Transactional
    public void dealStatusDone(Integer productNo, Integer boughtMemberNo) {
        log.info("dealStatusDone(productNo = {}, boughtMemberNo = {})", productNo, boughtMemberNo);

        Product product = productRepository.findById(productNo).get();
        log.info("product = {}", product);

        product.updateStatusAndBoughtMemberNo(true, boughtMemberNo);
    }

    public Product isDealStatus(Integer productNo) {
        log.info("isDealStatus(productNo = {})", productNo);

        Product product = productRepository.findById(productNo).get();
        log.info("product = {}", product);

        return product;
    }


    @Transactional
    public Integer update(ProductUpdateDto dto) { // 상품 업데이트.
            log.info("update(dto={})", dto);

            Product entity = productRepository.findById(dto.getProductNo()).get();
            Product newProduct = entity.update(dto.getTitle(), dto.getContent(), dto.getPrice(), dto.getCategory());
            log.info("newProduct={}");

            return entity.getProductNo();
        }

    public Integer delete(Integer productNo) {
            log.info("deleteProduct(productNo={})", productNo);

            Product product = productRepository.findById(productNo).get();
            log.info("product = {}", product);

            //매너온도 - 2.5
            product.getMember().gradeUpdate(product.getMember().getGrade() - 2.5);

            ProductOfInterested interestedProduct = productOfInterestedRepository.findByProduct(product);
            log.info("interestedProduct = {}", interestedProduct);

            if (interestedProduct != null) { // 찜 목록에 데이터가 있는 경우
                log.info("not null");
                productOfInterestedRepository.deleteById(interestedProduct.getNo()); // 찜 목록에 상품 번호 전부 삭제
                productRepository.deleteById(productNo); // 상품 테이블 해당 번호로 삭제
            } else { // 찜 목록에 데이터가 없는 경우
                log.info("null");
                productRepository.deleteById(productNo); // 상품 테이블 해당 번호로 삭제
            }

            return productNo;
        }


    public String saveImage(MultipartFile file) throws Exception {
        String projectFilePath = "/images/product/" + file.getOriginalFilename();
        log.info("projectFilePath={}", projectFilePath);

        String projectPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\images\\product";  // 저장할 경로 지정
        log.info("productPath()" + projectPath);

        File saveFile = new File(projectPath, file.getOriginalFilename());

        file.transferTo(saveFile);
        return file.getOriginalFilename();
    }

    @Transactional
    public Product create(Product product, MultipartFile file) throws Exception {
        String fileName = saveImage(file);

        if (product.getPhotoUrl1() == null) {
            product.setPhotoUrl1("/images/product/" + fileName);
            product.setPhotoName1(fileName);
        } else if (product.getPhotoUrl2() == null) {
            product.setPhotoUrl2("/images/product/" + fileName);
            product.setPhotoName2(fileName);
        } else if (product.getPhotoUrl3() == null) {
            product.setPhotoUrl3("/images/product/" + fileName);
            product.setPhotoName3(fileName);
        } else if (product.getPhotoUrl4() == null) {
            product.setPhotoUrl4("/images/product/" + fileName);
            product.setPhotoName4(fileName);
        } else if (product.getPhotoUrl5() == null) {
            product.setPhotoUrl5("/images/product/" + fileName);
            product.setPhotoName5(fileName);
        } else if (product.getPhotoUrl1() == null && product.getPhotoUrl2() == null && product.getPhotoUrl3() == null && product.getPhotoUrl4() == product.getPhotoUrl5()) {
            product.setPhotoUrl1("/images/product");
        }

        return productRepository.save(product);
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

}




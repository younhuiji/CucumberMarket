package com.sohwakmo.cucumbermarket.repository;

import com.sohwakmo.cucumbermarket.domain.Member;
import com.sohwakmo.cucumbermarket.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

//     select * from PRODUCTS where lower(TITLE) like ? or lower (CONTENT) like ? order by PRODUCT_NO desc
    List<Product> findByTitleIgnoreCaseContainingOrContentIgnoreCaseContainingOrderByProductNoDesc(String title, String content);

    List<Product> findByMember(Member member);

}

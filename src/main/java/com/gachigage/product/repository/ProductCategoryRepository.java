package com.gachigage.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.gachigage.product.domain.ProductCategory;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

	/**
	 * 1차 카테고리 조회 (parent_id IS NULL)
	 */
	@Query("""
			SELECT pc
			FROM ProductCategory pc
			WHERE pc.parent IS NULL
			ORDER BY pc.id
		""")
	List<ProductCategory> findAllParents();

	/**
	 * 1차 + 2차 카테고리를 한 번에 조회 (N+1 방지)
	 */
	@Query("""
			SELECT DISTINCT pc
			FROM ProductCategory pc
			LEFT JOIN FETCH pc.children
			WHERE pc.parent IS NULL
			ORDER BY pc.id
		""")
	List<ProductCategory> findAllParentsWithChildren();

	List<ProductCategory> findByParentId(Long parentId);

}

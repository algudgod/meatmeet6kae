package com.meatmeet6kae.repository.imageFile;

import com.meatmeet6kae.entity.imageFile.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.*;

@Repository
public interface ImageFileRepository extends JpaRepository<ImageFile, Integer> {

}

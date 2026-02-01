package com.auggie.student_server.mapper;

import com.auggie.student_server.entity.TrainingPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
@org.springframework.stereotype.Repository
public interface TrainingPlanMapper {

    List<TrainingPlan> findAll();

    TrainingPlan findById(@Param("id") Long id);

    List<TrainingPlan> findByMajorAndCourse(@Param("majorId") Integer majorId, @Param("courseId") Integer courseId);

    List<TrainingPlan> findBySearch(@Param("majorId") Integer majorId, 
                                     @Param("courseId") Integer courseId,
                                     @Param("planVersion") String planVersion,
                                     @Param("courseType") String courseType,
                                     @Param("status") Integer status);

    int insert(@Param("tp") TrainingPlan trainingPlan);

    int updateById(@Param("tp") TrainingPlan trainingPlan);

    int deleteById(@Param("id") Long id);

    TrainingPlan findByMajorCourseVersion(@Param("majorId") Integer majorId, 
                                           @Param("courseId") Integer courseId, 
                                           @Param("planVersion") String planVersion);
}

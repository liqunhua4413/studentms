import Vue from 'vue';
import VueRouter from 'vue-router';
import login from '../views/login/index';
import admin from '../views/Admin/index';
import adminHome from '../views/Admin/home';
import studentManage from '../views/Admin/studentManage/index'
import addStudent from "@/views/Admin/studentManage/addStudent";
import studentList from "@/views/Admin/studentManage/studentList";
import editorStudent from "@/views/Admin/studentManage/editorStudent";
import importStudent from "@/views/Admin/studentManage/importStudent";
import teacherManage from "@/views/Admin/teacherManage/index"
import addTeacher from "@/views/Admin/teacherManage/addTeacher";
import editorTeacher from "@/views/Admin/teacherManage/editorTeacher";
import importTeacher from "@/views/Admin/teacherManage/importTeacher";
import courseManage from "@/views/Admin/courseManage/index";
import addCourse from "@/views/Admin/courseManage/addCourse";
import importCourse from "@/views/Admin/courseManage/importCourse";
import teacher from "@/views/Teacher/index";
import queryStudent from "@/views/Admin/studentManage/queryStudent";
import queryTeacher from "@/views/Admin/teacherManage/queryTeacher";
import student from "@/views/Student/index";
import editorCourse from "@/views/Admin/courseManage/editorCourse";
import courseList from "@/views/Admin/courseManage/courseList";
import queryCourse from "@/views/Admin/courseManage/queryCourse";
import offerCourse from "@/views/Teacher/offerCourse";
import teacherHome from "@/views/Teacher/home";
import setCourse from "@/views/Teacher/setCourse";
import studentHome from "@/views/Student/home";
import myOfferCourse from "@/views/Teacher/myOfferCourse";
import CourseTeacherManage from "@/views/Admin/selectCourseManage/index";
import queryCourseTeacher from "@/views/Admin/selectCourseManage/queryCourseTeacher";
import studentSelectCourseManage from "@/views/Student/selectCourse/index";
import selectCourse from "@/views/Student/selectCourse/selectCourse";
import querySelectedCourse from "@/views/Student/selectCourse/querySelectedCourse";
import studentCourseGrade from "@/views/Student/courseGrade/index";
import queryCourseGrade from "@/views/Student/courseGrade/queryCourseGrade";
import queryGradeCourse from "@/views/Admin/gradeCourseManage/queryGradeCourse";
import editorGradeCourse from "@/views/Admin/gradeCourseManage/editorGradeCourse";
import teacherGradeCourseManage from "@/views/Teacher/teacherGradeCourseManage/index";
import teacherQueryGradeCourse from "@/views/Teacher/teacherGradeCourseManage/teacherQueryGradeCourse";
import teacherGradeCourseList from "@/views/Teacher/teacherGradeCourseManage/teacherGradeCourseList";
import teacherEditorGradeCourse from "@/views/Teacher/teacherGradeCourseManage/teacherEditorGradeCourse";
import updateInfo from "@/components/updateInfo";
import departmentManage from "@/views/Admin/departmentManage/index";
import departmentList from "@/views/Admin/departmentManage/departmentList";
import addDepartment from "@/views/Admin/departmentManage/addDepartment";
import editorDepartment from "@/views/Admin/departmentManage/editorDepartment";
import queryDepartment from "@/views/Admin/departmentManage/queryDepartment";
import importDepartment from "@/views/Admin/departmentManage/importDepartment";
import majorManage from "@/views/Admin/majorManage/index";
import majorList from "@/views/Admin/majorManage/majorList";
import addMajor from "@/views/Admin/majorManage/addMajor";
import editorMajor from "@/views/Admin/majorManage/editorMajor";
import queryMajor from "@/views/Admin/majorManage/queryMajor";
import importMajor from "@/views/Admin/majorManage/importMajor";
import classManage from "@/views/Admin/classManage/index";
import classList from "@/views/Admin/classManage/classList";
import addClass from "@/views/Admin/classManage/addClass";
import editorClass from "@/views/Admin/classManage/editorClass";
import queryClass from "@/views/Admin/classManage/queryClass";
import importClass from "@/views/Admin/classManage/importClass";
import importCourseTeacher from "@/views/Admin/selectCourseManage/importCourseTeacher";
import gradeManage from "@/views/Admin/gradeManage/index";
import uploadGrade from "@/views/Admin/gradeManage/uploadGrade";
import queryGrade from "@/views/Admin/gradeManage/queryGrade";
import wordPaperManage from "@/views/Admin/wordPaperManage/index";
import wordPaperList from "@/views/Admin/wordPaperManage/wordPaperList";
import operationLogManage from "@/views/Admin/operationLogManage/index";

Vue.use(VueRouter)

const routes = [
  {
    // 随便定义的首页
    path: '/',
    name: 'index',
    component: login,
    redirect: '/login'
  },
  {
    // 登陆页
    path: '/login',
    name: 'login',
    component: login
  },
  {
    // admin 的路由
    path: '/admin',
    name: 'admin',
    redirect: '/adminHome',
    component: admin,
    meta: {requireAuth: true},
    children: [
      {
        path: '/adminHome',
        name: 'Hi! admin',
        component: adminHome,
        meta: {requireAuth: true},
        children: [
          {
            path: '/adminHome',
            name: 'admin 主页',
            component: adminHome,
            meta: {requireAuth: true},
          }
        ]
      },
      {
        path: '/studentManage',
        name: '学生管理',
        component: studentManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addStudent',
            name: '添加学生',
            component: addStudent,
            meta: {requireAuth: true}
          },
          {
            path: '/importStudent',
            name: '学生批量导入',
            component: importStudent,
            meta: {requireAuth: true}
          },
          {
            path: '/studentList',
            name: '学生列表',
            component: studentList,
            meta: {requireAuth: true},
          },
          {
            path: '/editorStudent',
            name: '编辑学生',
            component: editorStudent,
            meta: {requireAuth: true}
          },
          {
            path: '/queryStudent',
            name: '搜索',
            component: queryStudent,
            meta: {requireAuth: true},
            children: [
              {
                path: '/queryStudent/studentList',
                component: studentList,
                meta: {requireAuth: true}
              }
            ]
          }
        ]
      },
      {
        path: '/teacherManage',
        name: '教师管理',
        component: teacherManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addTeacher',
            name: '添加教师',
            component: addTeacher,
            meta: {requireAuth: true}
          },
          {
            path: '/importTeacher',
            name: '教师批量导入',
            component: importTeacher,
            meta: {requireAuth: true}
          },
          {
            path: '/queryTeacher',
            name: '教师列表',
            component: queryTeacher,
            meta: {requireAuth: true},
            children: [
            ]
          },
          {
            path: '/editorTeacher',
            name: '编辑教师',
            component: editorTeacher,
            meta: {requireAuth: true}
          },
        ]
      },
      {
        path: '/courseManage',
        name: '课程管理',
        component: courseManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addCourse',
            name: '添加课程',
            component: addCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/importCourse',
            name: '课程批量导入',
            component: importCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/queryCourse',
            name: '搜索课程',
            component: queryCourse,
            meta: {requireAuth: true},
            children: [
              {
                path: '/courseList',
                name: '课程列表',
                component: courseList,
                meta: {requireAuth: true}
              },
            ]
          },
          {
            path: '/editorCourse',
            name: '编辑课程',
            component: editorCourse,
            meta: {requireAuth: true}
          },
        ]
      },
      {
        path: '/CourseTeacher',
        name: '开课表管理',
        component: CourseTeacherManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/queryCourseTeacher',
            name: '开课管理',
            component: queryCourseTeacher,
            meta: {requireAuth: true},
          },
          {
            path: '/importCourseTeacher',
            name: '批量导入',
            component: importCourseTeacher,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/departmentManage',
        name: '学院管理',
        component: departmentManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addDepartment',
            name: '添加学院',
            component: addDepartment,
            meta: {requireAuth: true}
          },
          {
            path: '/importDepartment',
            name: '学院批量导入',
            component: importDepartment,
            meta: {requireAuth: true}
          },
          {
            path: '/departmentList',
            name: '学院列表',
            component: departmentList,
            meta: {requireAuth: true}
          },
          {
            path: '/editorDepartment',
            name: '编辑学院',
            component: editorDepartment,
            meta: {requireAuth: true}
          },
          {
            path: '/queryDepartment',
            name: '搜索学院',
            component: queryDepartment,
            meta: {requireAuth: true},
            children: [
              {
                path: '/queryDepartment/departmentList',
                component: departmentList,
                meta: {requireAuth: true}
              }
            ]
          }
        ]
      },
      {
        path: '/majorManage',
        name: '专业管理',
        component: majorManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addMajor',
            name: '添加专业',
            component: addMajor,
            meta: {requireAuth: true}
          },
          {
            path: '/importMajor',
            name: '专业批量导入',
            component: importMajor,
            meta: {requireAuth: true}
          },
          {
            path: '/majorList',
            name: '专业列表',
            component: majorList,
            meta: {requireAuth: true}
          },
          {
            path: '/editorMajor',
            name: '编辑专业',
            component: editorMajor,
            meta: {requireAuth: true}
          },
          {
            path: '/queryMajor',
            name: '搜索专业',
            component: queryMajor,
            meta: {requireAuth: true},
            children: [
              {
                path: '/queryMajor/majorList',
                component: majorList,
                meta: {requireAuth: true}
              }
            ]
          }
        ]
      },
      {
        path: '/classManage',
        name: '班级管理',
        component: classManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/addClass',
            name: '添加班级',
            component: addClass,
            meta: {requireAuth: true}
          },
          {
            path: '/importClass',
            name: '班级批量导入',
            component: importClass,
            meta: {requireAuth: true}
          },
          {
            path: '/classList',
            name: '班级列表',
            component: classList,
            meta: {requireAuth: true}
          },
          {
            path: '/editorClass',
            name: '编辑班级',
            component: editorClass,
            meta: {requireAuth: true}
          },
          {
            path: '/queryClass',
            name: '搜索班级',
            component: queryClass,
            meta: {requireAuth: true},
            children: [
              {
                path: '/queryClass/classList',
                component: classList,
                meta: {requireAuth: true}
              }
            ]
          }
        ]
      },
      {
        path: '/gradeManage',
        name: '成绩管理',
        component: gradeManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/uploadGrade',
            name: '成绩上传',
            component: uploadGrade,
            meta: {requireAuth: true}
          },
          {
            path: '/queryGrade',
            name: '成绩查询',
            component: queryGrade,
            meta: {requireAuth: true}
          },
          {
            path: '/editorGradeCourse',
            name: '编辑成绩',
            component: editorGradeCourse,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/wordPaperManage',
        name: '试卷分析',
        component: wordPaperManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/wordPaperList',
            name: '文件列表',
            component: wordPaperList,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/operationLogManage',
        name: '操作日志',
        component: operationLogManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/operationLogList',
            name: '日志查看',
            component: operationLogManage,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/otherFunctions',
        name: '系统维护',
        component: () => import('@/views/Admin/otherFunctions/index'),
        meta: {requireAuth: true},
        children: [
          {
            path: '/dataMaintenance',
            name: '数据清空/生成',
            component: () => import('@/views/Admin/otherFunctions/index'),
            meta: {requireAuth: true}
          }
        ]
      }
    ]
  },
  {
    path: '/dean',
    name: 'dean',
    component: admin, // 使用 admin 的布局
    redirect: '/deanHome',
    meta: {requireAuth: true},
    children: [
      {
        path: '/deanHome',
        name: 'Hi! Dean',
        component: adminHome,
        meta: {requireAuth: true},
        children: [
          {
            path: '/deanHome',
            name: '院长主页',
            component: adminHome,
            meta: {requireAuth: true},
          }
        ]
      },
      {
        path: '/deanGradeManage',
        name: '成绩管理',
        component: gradeManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/deanUploadGrade',
            name: '成绩上传',
            component: uploadGrade,
            meta: {requireAuth: true}
          },
          {
            path: '/deanQueryGrade',
            name: '成绩查询',
            component: queryGrade,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/deanWordPaperManage',
        name: '试卷分析',
        component: wordPaperManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/deanWordPaperList',
            name: '文件列表',
            component: wordPaperList,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/deanOperationLogManage',
        name: '操作日志',
        component: operationLogManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/deanOperationLogList',
            name: '日志查看',
            component: operationLogManage,
            meta: {requireAuth: true}
          }
        ]
      }
    ]
  },
  {
    path: '/teacher',
    name: 'teacher',
    component: teacher,
    redirect: '/teacherHome',
    meta: {requireAuth: true},
    children: [
      {
        path: '/teacherHome',
        name: 'Hi! teacher',
        meta: {requireAuth: true},
        component: teacherHome,
        children: [
          {
            path: '/teacherHome',
            name: '教师主页',
            meta: {requireAuth: true},
            component: teacherHome
          },
        ]
      },
      {
        path: '/updateInfo',
        name: '教师编辑',
        component: updateInfo,
        meta: {requireAuth: true},
        children: [
          {
            path: '/updateInfoHome',
            name: '编辑教师信息',
            component: updateInfo,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/courseManage',
        name: '课程设置',
        meta: {requireAuth: true},
        component: setCourse,
        children: [
          {
            path: '/myOfferCourse',
            name: '我开设的课程',
            component: myOfferCourse,
            meta: {requireAuth: true}
          },
          // 教师不能开设课程，只有管理员可以
          // {
          //   path: '/offerCourse',
          //   name: '开设课程',
          //   component: offerCourse,
          //   meta: {requireAuth: true}
          // },
        ]
      },
      {
        name: '教师成绩管理',
        path: '/teacherQueryGradeCourseManage',
        component: teacherGradeCourseManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/teacherQueryGradeCourseManage',
            name: '成绩管理',
            component: teacherQueryGradeCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/teacherUploadGrade',
            name: '上传成绩单',
            component: () => import('@/views/Teacher/teacherGradeCourseManage/teacherUploadGrade'),
            meta: {requireAuth: true}
          },
          {
            path: '/teacherUploadWordPaper',
            name: '上传试卷分析',
            component: () => import('@/views/Teacher/teacherGradeCourseManage/teacherUploadWordPaper'),
            meta: {requireAuth: true}
          },
          {
            path: '/teacherUploadRecordList',
            name: '已上传成绩单',
            component: () => import('@/views/Teacher/teacherGradeCourseManage/teacherUploadRecordList'),
            meta: {requireAuth: true}
          },
          {
            path: '/teacherWordPaperList',
            name: '已上传试卷分析',
            component: () => import('@/views/Teacher/teacherGradeCourseManage/teacherWordPaperList'),
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/teacherOperationLogManage',
        name: '操作日志',
        component: operationLogManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/teacherOperationLogList',
            name: '日志查看',
            component: operationLogManage,
            meta: {requireAuth: true}
          }
        ]
      }
    ]
  },
  {
    path: '/student',
    name: 'student',
    component: student,
    redirect: '/studentHome',
    meta: {requireAuth: true},
    children: [
      {
        path: '/student',
        name: 'hi! student',
        component: studentHome,
        meta: {requireAuth: true},
        children: [
          {
            path: '/studentHome',
            name: '学生主页',
            component: studentHome,
            meta: {requireAuth: true},
          },
        ],
      },
      {
        path: '/updateInfo',
        name: '学生编辑',
        component: updateInfo,
        meta: {requireAuth: true},
        children: [
          {
            path: '/updateInfoHome',
            name: '编辑学生信息',
            component: updateInfo,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/studentSelectCourseManage',
        name: '选课管理',
        component: studentSelectCourseManage,
        meta: {requireAuth: true},
        children: [
          {
            path: '/studentSelectCourse',
            name: '选课',
            component: selectCourse,
            meta: {requireAuth: true}
          },
          {
            path: '/querySelectedCourse',
            name: '查询课表',
            component: querySelectedCourse,
            meta: {requireAuth: true}
          }
        ]
      },
      {
        path: '/courseGrade',
        name: '学生成绩管理',
        component: studentCourseGrade,
        meta: {requireAuth: true},
        children: [
          {
            path: '/queryCourseGrade',
            name: '成绩查询',
            component: queryCourseGrade,
            meta: {requireAuth: true}
          },
        ]
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router

/*
  session 设置：
    1. token
    2. name
    3. type
    4. tid
    5. sid
    5. 系统信息 info
 */
router.beforeEach((to, from, next) => {
  console.log(from.path + ' ====> ' + to.path)
  if (to.meta.requireAuth) { // 判断该路由是否需要登录权限
    if (sessionStorage.getItem("token") === 'true') { // 判断本地是否存在token
      next()
    } else {
      // 未登录,跳转到登陆页面
      next({
        path: '/login',
        query: {redirect: to.fullPath}
      })
    }
  } else {
    // 不需要登陆权限的页面，如果已经登陆，则跳转主页面
    if(sessionStorage.getItem("token") === 'true'){
      console.log('检查拦截器配置，大概率出现漏网之鱼')
      const t = sessionStorage.getItem("type")
      next('/' + t);
    }else{
      next();
    }
  }
});
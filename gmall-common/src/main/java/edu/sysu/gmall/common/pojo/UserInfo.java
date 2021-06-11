package edu.sysu.gmall.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-02 15:22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private String userKey;
    private String userId;
}

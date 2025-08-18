package com.boardly.infrastructure.adapters.out.persistence.workspace;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkspaceJpaRepository extends JpaRepository<WorkspaceEntity, String> {

    @Query(value = "\n" +
        "select w.* from workspaces w\n" +
        "where w.owner_user_id = :userId\n" +
        "   or exists (\n" +
        "       select 1 from workspace_members wm\n" +
        "       where wm.workspace_id = w.id and wm.user_id = :userId and wm.invite_status = 'ACCEPTED'\n" +
        "   )\n",
        nativeQuery = true)
    List<WorkspaceEntity> findAllAccessibleByUser(@Param("userId") String userId);
}

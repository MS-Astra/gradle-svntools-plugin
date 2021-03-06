package at.bxm.gradleplugins.svntools.tasks

import at.bxm.gradleplugins.svntools.SvnTestSupport
import org.gradle.api.Project
import org.gradle.api.tasks.TaskExecutionException

class SvnBranchTest extends SvnTestSupport {

  File workspace
  Project project
  SvnBranch task

  def setup() {
    createLocalRepo()
    project = projectWithPlugin()
    task = project.task(type: SvnBranch, "task") as SvnBranch
  }

  def "branch from trunk"() {
    given: "a trunk workspace"
    workspace = checkoutTrunk()

    when: "running the task"
    task.workspaceDir = workspace
    task.branchName = "branch"
    task.execute()

    then: "branch exists"
    switchLocalRepo("branches/branch")
    getRevision(workspace) == 2
  }

  def "branch from a branch"() {
    given: "a branch workspace"
    workspace = checkoutBranch()

    when: "running the task"
    task.workspaceDir = workspace
    task.branchName = "v2.0"
    task.execute()

    then: "branch exists"
    switchLocalRepo("branches/v2.0")
    getRevision(workspace) == 2
  }

  def "branch already exists"() {
    given: "a trunk workspace"
    workspace = checkoutTrunk()

    when: "creating an existing branch"
    task.workspaceDir = workspace
    task.branchName = "test-branch"
    task.execute()

    then:
    def e = thrown TaskExecutionException
    e.cause.message =~ /.* already exists/
  }

  def "branch already exists - replace"() {
    given: "a trunk workspace"
    workspace = checkoutTrunk()

    when: "creating an existing branch"
    task.workspaceDir = workspace
    task.branchName = "test-branch"
    task.replaceExisting = true
    task.execute()

    then: "two more commits (delete and copy)"
    switchLocalRepo("branches/test-branch")
    getRevision(workspace) == 3
  }
}

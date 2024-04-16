package gclaramunt.asbchallenge

import github4s.domain.PullRequest

case class PRData(id: Long,  projectUser: String, projectRepo: String, userId: Long, status: String)

object PRData {
  def from(projectUser: String, projectRepo: String, ghPr: PullRequest): PRData = PRData(ghPr.id, projectUser, projectRepo , ghPr.user.get.id, ghPr.state)
}
